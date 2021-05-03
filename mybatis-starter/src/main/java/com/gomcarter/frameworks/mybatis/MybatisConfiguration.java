package com.gomcarter.frameworks.mybatis;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.gomcarter.frameworks.mybatis.datasource.ReadWriteDataSource;
import com.gomcarter.frameworks.mybatis.datasource.ReadWriteDataSourceProcessor;
import com.gomcarter.frameworks.mybatis.injector.CustomSqlInjector;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.aop.aspectj.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Collections;
import java.util.List;

/**
 * mybatis配置
 *
 * @author 李银 2020年03月17日17:17:33
 */
@Aspect
@Component
public class MybatisConfiguration {

    /**
     * 支持子包来决定使用什么injector
     */
    public static class Holder {
        public static ISqlInjector injector;

        static {
            List<ISqlInjector> injectorList = SpringFactoriesLoader.loadFactories(ISqlInjector.class, null);
            if (CollectionUtils.isEmpty(injectorList)) {
                injector = new CustomSqlInjector();
            } else {
                injector = injectorList.stream()
                        .min((a, b) -> {
                            int aSort = 0;
                            int bSort = 0;
                            if (a.getClass().isAnnotationPresent(Order.class)) {
                                aSort = a.getClass().getAnnotation(Order.class).value();
                            }
                            if (b.getClass().isAnnotationPresent(Order.class)) {
                                bSort = b.getClass().getAnnotation(Order.class).value();
                            }
                            return aSort - bSort;
                        })
                        .orElse(new CustomSqlInjector());
            }
        }
    }

    /**
     * @param source source
     * @return SqlSessionFactoryBean
     * @throws Exception Exception
     */
    @Bean
    public MybatisSqlSessionFactoryBean sqlSessionFactoryBean(ReadWriteDataSource source) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(source);

        // 自定义 sql 注入器
        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.setSqlInjector(Holder.injector);
        factoryBean.setGlobalConfig(globalConfig);

        // 分页插件
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 暂时不支持别的参数配置
        paginationInterceptor.setDbType(MybatisConfigHolder.DB_TYPE);
        factoryBean.setPlugins(paginationInterceptor);

        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources(MybatisConfigHolder.DAO_XML_PATH)
        );
        return factoryBean;
    }

    /**
     * @return MapperScannerConfigurer
     */
    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setBasePackage(StringUtils.join(MybatisConfigHolder.DAO_BASE_PACKAGE, ","));
        configurer.setSqlSessionFactoryBeanName("sqlSessionFactoryBean");
        return configurer;
    }

    /**
     * @return NameMatchTransactionAttributeSource
     */
    @Bean
    public NameMatchTransactionAttributeSource nameMatchTransactionAttributeSource() {
        /*只读事务，不做更新操作*/
        RuleBasedTransactionAttribute readOnlyTx = new RuleBasedTransactionAttribute();
        readOnlyTx.setReadOnly(true);
        readOnlyTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        /*当前存在事务就使用当前事务，当前不存在事务就创建一个新的事务*/
        RuleBasedTransactionAttribute requiredTx = new RuleBasedTransactionAttribute();
        requiredTx.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
        requiredTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        NameMatchTransactionAttributeSource attributeSource = new NameMatchTransactionAttributeSource();
        for (String pattern : MybatisConfigHolder.TRANSACTION_REQUIRED_NAME_MAP) {
            attributeSource.addTransactionalMethod(pattern, requiredTx);
        }
        attributeSource.addTransactionalMethod("*", readOnlyTx);

        return attributeSource;
    }

    /**
     * @param attributeSource attributeSource above
     * @return ReadWriteDataSourceProcessor
     */
    @Bean
    public ReadWriteDataSourceProcessor readWriteDataSourceProcessor(NameMatchTransactionAttributeSource attributeSource) {
        ReadWriteDataSourceProcessor processor = new ReadWriteDataSourceProcessor();
        processor.setForceChoiceReadWhenWrite(false);
        processor.postProcessAfterInitialization(attributeSource, "nameMatchTransactionAttributeSource");
        return processor;
    }

    /**
     * 事务拦截器
     *
     * @param dataSource      dataSource
     * @param attributeSource attributeSource
     * @return AspectJExpressionPointcutAdvisor
     */
    @Bean
    public AspectJExpressionPointcutAdvisor transactionAdvisor(ReadWriteDataSource dataSource, NameMatchTransactionAttributeSource attributeSource) {
        /* 事务管理器 */
        TransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

        /* 事务切面 */
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setAdvice(new TransactionInterceptor(transactionManager, attributeSource));
        advisor.setExpression(MybatisConfigHolder.TRANSACTION_POINTCUT_EXPRESSION);

        return advisor;
    }

    /**
     * 主库、从库选择AOP
     *
     * @param readWriteDataSourceProcessor readWriteDataSourceProcessor
     * @return AspectJExpressionPointcutAdvisor
     * @throws NoSuchMethodException NoSuchMethodException
     */
    @Bean
    public AspectJExpressionPointcutAdvisor determineReadOrWriteDBAdvisor(ReadWriteDataSourceProcessor readWriteDataSourceProcessor) throws NoSuchMethodException {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(MybatisConfigHolder.TRANSACTION_POINTCUT_EXPRESSION);
        advisor.setOrder(Integer.MIN_VALUE);

        AspectInstanceFactory aspectInstanceFactory = new SingletonAspectInstanceFactory(readWriteDataSourceProcessor);
        AspectJAroundAdvice aroundAdvice = new AspectJAroundAdvice(ReadWriteDataSourceProcessor.class.getMethod("determineReadOrWriteDB", ProceedingJoinPoint.class),
                (AspectJExpressionPointcut) advisor.getPointcut(),
                aspectInstanceFactory);

        advisor.setAdvice(aroundAdvice);
        return advisor;
    }
}
