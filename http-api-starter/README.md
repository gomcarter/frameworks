在你的项目中引入依赖: https://mvnrepository.com/artifact/com.gomcarter.frameworks/redis-starter

### 使用指南（目前仅支持多种配置中心，也可自行注入配置中心，<a href="https://github.com/gomcarter/developer/blob/master/README.md">配置中心配置参考</a>）



# 核心功能

### API方式封装 rpc

##### 服务提供方：
```
@RestController
@RequestMapping("public/foo")
public class FooController {

    @GetMapping(value = "getById", name = "接口")
    Foo getById(Long id) {
        return this.fooService.getById(id);
    }
}
```

##### 创建一个 jar 包
```
import com.gomcarter.frameworks.httpapi.api.AbstractConfigurableApi

public class DemoApi extends NacosConfigurableApi {
    
    @Override
    public NacosConfig getDiamondConfig() {
        // 配置中心存储 http 的服务地址 如：
        // demo.api.getbyid=http://fooserver/public/foo/getById
        return NacosConfig.valueOf("GROUP", "DATA_ID");
    }

    public Foo getById(Long id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        
        // 更多方法见：com.gomcarter.frameworks.httpapi.api.BaseApi
        return this.get("demo.api.getbyid", Foo.class, params);
    }
}
```

##### 调用方
```
注入一个 DemoApi 为一个 bean
@Configuration
public class Configuration {
  @Bean
  DemoApi demoApi() {
    return new DemoApi();
  }
}

使用
@Service
public class BarService {
  @Autowired
  DemoApi demoApi;
  
  public void funtion() {
    // ...
    Foo foo = demoApi.getById(1L);
    // ...
  }
}
```


### 注解方式封装 rpc
##### 服务提供方：
```
@RestController
@RequestMapping("public/foo")
public class FooController {

    @GetMapping(value = "getById", name = "接口")
    Foo getById(Long id) {
        return this.fooService.getById(id);
    }
}
```

##### 创建一个 jar 包
```
package com.gomcarter.frameworks.httpapi.demo;

import com.gomcarter.frameworks.httpapi.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author gomcarter
 */
@EnableHttp(dataId = "MEMBER", group = "API")
public interface HttpDemoApi {

    @HttpMethod(method = Method.GET, key = "get.by.idList")
    Foo getById(@HttpParam("id") Long id);

    default Foo get() {
        return this.get(1L);
    }
}
```

##### 调用方
```
使用

@Service
public class BarService {
  @HttpResource
  HttpDemoApi demoApi;
  
  public void funtion() {
    // ...
    Foo foo = demoApi.getById(1L);
    // ...
  }
}
```
##### demo：<a href="https://github.com/gomcarter/frameworks/tree/master/http-api-starter/src/main/java/com/gomcarter/frameworks/httpapi/demo">戳这里</a>
