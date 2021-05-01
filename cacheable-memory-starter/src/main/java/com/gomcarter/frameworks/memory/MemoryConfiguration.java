package com.gomcarter.frameworks.memory;

import com.gomcarter.frameworks.memory.aop.DataMemoryInterceptor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class MemoryConfiguration {

    private DataMemoryInterceptor dataRedisInterceptor;

    /**
     * @return DataMemoryInterceptor
     */
    @Bean
    @ConditionalOnMissingBean
    public DataMemoryInterceptor dataRedisInterceptor() {
        dataRedisInterceptor = new DataMemoryInterceptor();
        return dataRedisInterceptor;
    }

    @Pointcut("@annotation(com.gomcarter.frameworks.cache.annotation.Cache)")
    public void cacheData() {
    }

    @Around("cacheData()")
    public Object cacheData(ProceedingJoinPoint joinPoint) throws Throwable {
        return dataRedisInterceptor.cacheData(joinPoint);
    }

    @Pointcut("@annotation(com.gomcarter.frameworks.cache.annotation.DelCache)")
    public void delCache() {
    }

    @AfterReturning("delCache()")
    public void delCache(JoinPoint joinPoint) throws Throwable {
        dataRedisInterceptor.dropCache(joinPoint);
    }

    @Pointcut("@annotation(com.gomcarter.frameworks.cache.annotation.Lock)")
    public void lockCache() {
    }

    @Around("lockCache()")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        return dataRedisInterceptor.lock(joinPoint);
    }

}
