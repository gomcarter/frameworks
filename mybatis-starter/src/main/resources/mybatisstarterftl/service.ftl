package ${entity.servicePackageName};

import com.gomcarter.frameworks.mybatis.service.BaseService;
import org.springframework.stereotype.Service;
import ${entity.daoPackageName}.${entity.className}Mapper;
import com.gomcarter.frameworks.mybatis.mapper.BaseMapper;
import ${entity.entityPackageName}.${entity.className};

import javax.annotation.Resource;

/**
 * @author ${entity.author} on ${entity.createTime}
 */
@Service
public class ${entity.className}Service implements BaseService<${entity.className}> {

    @Resource
    ${entity.className}Mapper ${entity.classInstanceName}Mapper;

    @Override
    public BaseMapper<${entity.className}> mapper() {
        return ${entity.classInstanceName}Mapper;
    }

}
