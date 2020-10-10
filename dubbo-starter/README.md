### Apollo中配置
* 建议放在apollo中叫做op.dubbo的namespace中，方便管理，可以放在单独项目中
* 每个项目可以根据需要覆盖或者增加更多选项，具体参考dubbo官方文档
```properties
# 该处默认取app.id的值，如果需要更改可以关联该配置覆盖即可
dubbo.application.name = ${app.id:unknown}
dubbo.registry.address = zookeeper://127.0.0.1:2181
dubbo.protocol.name = dubbo
# 暴露端口，如果多项目部署在同一个虚拟机中，对应项目也需要覆盖该选项
dubbo.protocol.port = 20880
dubbo.consumer.timeout = 3000
```

### 项目中使用
* 参考官方annotation注解使用配置方式，由于我们通过apollo加载配置，所以不需要在properties里做对应配置，如果需要请按照格式在apollo里关联扩展
* http://dubbo.apache.org/zh-cn/docs/user/configuration/annotation.html
```java
@Configuration
@EnableDubbo(scanBasePackages = "com.gomcarter.**.dubbo")
public class DubboConfig {
}
```
