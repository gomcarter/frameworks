package ${entity.servicePackageName};

import com.gomcarter.frameworks.mybatis.service.BaseService;
import org.springframework.stereotype.Service;
import ${entity.daoPackageName}.${entity.className}Mapper;
import ${entity.entityPackageName}.${entity.className};

import javax.annotation.Resource;

/**
 * @author ${entity.author} on ${entity.createTime}
 */
@Service
public class ${entity.className}Service extends BaseService<${entity.className}Mapper, ${entity.className}> {
    // 类型${entity.className}Mapper已自动注入，请使用 this.baseMapper
}
