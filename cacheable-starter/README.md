在你的项目中引入依赖: https://mvnrepository.com/artifact/com.gomcarter.frameworks/cacheable-redis-starter

### 使用指南（目前仅支持多种配置中心，也可自行注入配置中心，<a href="https://github.com/gomcarter/developer/blob/master/README.md">配置中心配置参考</a>）

# 核心功能

### 缓存、删除缓存、分布式锁

```

import com.gomcarter.frameworks.cache.annotation.Cache;
import com.gomcarter.frameworks.cache.annotation.DelCache;
import com.gomcarter.frameworks.cache.annotation.Lock;

@Service
public class FooService {

    @Resource
    private FooMapper fooMapper;
    
    @Cache(key = "FooService.getById", time = -1L, argsIndex = {0}, nullable = true, await = 2000)
    // @Cache -- 默认 key 为：当前类 ${package}.FooService.getById_MD5(args), 缓存5分钟，所有参数参与 key 生成，可以缓存null 值，如果有其他线程(或集群中其他节点)在执行此方法还没有出结果，就等待10秒，过期报错
    public Foo getById(Long id) {
        return fooMapper.getById(id);
    }
    
    // 方法执行完毕之后删除缓存 FooService.getById_MD5(id)
    @DelCache(key = "FooService.getById", argsIndex = {0})
    public void update(Long id, Foo foo) {
        this.fooMapper.update(...);
    }
    
    // 如果有其他线程(或集群中其他节点)在执行此方法，则报错；如果设置 await 字段则等待设置对应的时间
    @Lock
    public Foo create(Foo foo) {
        this.fooMapper.create(...);
    }
}
```
