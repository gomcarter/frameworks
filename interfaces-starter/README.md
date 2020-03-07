# developer #
### 一、引入jar包

在你的项目中引入
```
<dependency>
    <groupId>com.gomcarter.frameworks</groupId>
    <artifactId>interfaces-starter</artifactId>
    <version>${frameworks.version}</version>
</dependency>
```

见：<a href="https://mvnrepository.com/artifact/com.gomcarter.frameworks/interfaces-starter" target="_blank">mvn 中央仓库地址</a>

### 二、埋点

**a, 接口名称**

name将被读取到接口中心作为接口名称，没有设置接口名称将不会被接口中心读取到（有助于您想有些接口不想读入接口中心）
```
    @GetMapping(value = "list", name = "接口名称")
    private List<xx> list() {
    }
    
    @RequestMapping(value = "list", name = "接口名称，如果名称为空，则此接口不会存入接口中心")
    private List<xx> list() {
    }
```


**b，接口描述**

@Notes中的备注作为接口的描述
```
    import com.gomcarter.frameworks.interfaces.annotation.Notes
    
    @Notes("this interface for user login")
    @GetMapping(value = "list",name = "接口名称")
    public ReturnDto list(@Notes("id") Long id, Params params) {
    }
```  


**c，接口参数、返回值**
```
    import com.gomcarter.frameworks.interfaces.annotation.Notes
    
    @GetMapping(value = "list",name = "接口名称")
    public ReturnDto list(@Notes("id") Long id, Params params) {
        // id为方法上直接带参数，Params作为参数封装类
    }
    
    // 参数类，Notes中的备注将被接口中心读取作为接口参数字段描述
    class Params {
        // 对应字段直接复制，接口中心将读取作为参数的默认值
        @Notes(value = "xxxx", notNull = true)
        private String name = "默认值";
      
        @Notes("yyyy")
        private List<ParamsAA> list;
    }
  
    // 返回值类，Notes中的备注将被接口中心读取作为接口返回字段描述
    class ReturnDto {
        @Notes(value = "xxxx", notNull = true)
        private String name;
      
        @Notes("yyyy")
        private List<ReturnDtoAA> list;
    }
```


**d，启动服务**
```
    java -jar -Xms1g -Xmx2g -Dserver.port=自己服务端口 -Dinterfaces.domain=接口中心地址 -Dinterfaces.javaId=下面配置的java模块id xx-project.jar &

    -Dinterfaces.domain：接口中心地址
    -Dinterfaces.javaId：配置后台模块（java）id
```


### 三、兼容swagger

    自动读取如下注解配置：
    ApiOperation
    ApiParam
    ApiModelProperty
    
