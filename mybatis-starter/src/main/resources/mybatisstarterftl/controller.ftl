package ${entity.actionPackageName};

import ${entity.entityPackageName}.${entity.className};
import ${entity.paramPackage}.*;
import ${entity.dtoPackageName}.${entity.className}Dto;
import ${entity.servicePackageName}.${entity.className}Service;
import com.gomcarter.frameworks.base.common.AssertUtils;
import com.gomcarter.frameworks.base.common.BeanUtils;
import com.gomcarter.frameworks.base.pager.DefaultPager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ${entity.author} on ${entity.createTime}
 */
@RestController
@RequestMapping("${entity.classInstanceName}")
public class ${entity.className}Controller {

	@Resource
    private ${entity.className}Service ${entity.classInstanceName}Service;

    @GetMapping(value = "list", name = "分页查询")
    List<${entity.className}Dto> query(${entity.className}Param params, DefaultPager pager) {
        return this.${entity.classInstanceName}Service.query(params, pager)
                .stream()
                .map(s -> BeanUtils.copyObject(s, new ${entity.className}Dto()))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "count", name = "分页查询计算总数")
    Integer count(${entity.className}Param params) {
        return this.${entity.classInstanceName}Service.count(params);
    }

    @PostMapping(value = "create", name = "创建${entity.tableName}")
    ${entity.idSimpleType} create(${entity.className}CreateParam params) {
        ${entity.className} entity = BeanUtils.copyObject(params, new ${entity.className}());
        this.${entity.classInstanceName}Service.insert(entity);
        return entity.${entity.idGetMethod}();
    }

    @PostMapping(value = "update", name = "修改${entity.tableName}")
    void update(${entity.className}UpdateParam params) {
        ${entity.className} entity = this.${entity.classInstanceName}Service.getById(params.${entity.idGetMethod}());
        AssertUtils.notNull(entity);
        this.${entity.classInstanceName}Service.insert(BeanUtils.copyObject(params, entity));
    }

    @GetMapping(value = "detail", name = "查询${entity.tableName}详情")
    ${entity.className}Dto detail(@RequestParam ${entity.idSimpleType} ${entity.idName}) {
        ${entity.className} entity = this.${entity.classInstanceName}Service.getById(${entity.idName});
        return BeanUtils.copyObject(entity, new ${entity.className}Dto());
    }
}
