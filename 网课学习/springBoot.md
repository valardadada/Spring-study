# SpringBoot

## 特性

- 快速创建基于Spring的应用
- 能够直接使用java main方法启动内嵌的Tomcat服务器运行Spring Boot程序，不需要部署war包文件
- 提供约定的starter POM来简化Maven配置，让Maven配置更加简单。
- 自动化配置，根据项目的Maven依赖配置，Spring Boot自动配置Spring，Spring MVC等
- 提供程序的健康检查等功能
- 基本可以完全不使用XML配置文件，采用注解配置。

**四大核心**：

- 自动配置
- 起步依赖
- Actuator
- 命令行界面

##  起步

```java
//springboot项目启动入口类
@SpringBootApplication//springboot核心注解，用于开启spring自动配置
public class Application{
    
    public static void main(String[] args){
		SpringApplication.run(Application.class,args);
    }
}
```

**项目结构：**

/resources/static：静态资源，图片，js，cs，图标等

/resources/templates：前端模板，官推Thymeleaf（百叶香）

/resources/application.properties：springboot核心配置文件

**注意**：springboot项目得代码，必须放在application.java类的同级，或者更下的目录。

## application.properties配置

```properties
#设置内嵌Tomcat端口号
server.port=8081
#设置上下文根，访问的时候需要localhost:8081/springboot/xxx
server.servlet.context-path=/springboot
```

使用application.yml或者application.yaml配置：

采用空格和制表符来控制层级，例如port后，8081前一定要有空格

```yml
server:
	port: 8081
  	servlet: 
  		context-path: /springboot
```

配置内容没有区别，只是格式不同。

**如果多种核心配置文件存在**：如果application.properties，application.yml和application.yaml都存在，似乎不同版本的springboot有不同的优先级，需要实际测试。

**多环境配置文件**：在不同的环境（开发，测试，发布等环境）下的配置：

/resources/application-dev.properties

/resources/application-ready.properties

/resources/application-test.properties

/resources/application-product.properties

...使用application-xxx.properties来定义不同环境下的配置

在主核心配置文件application.properties中激活配置文件：

```properties
#使用application-text.properties这个配置文件
server.profiles.active=test
```

**自定义配置文件中的内容，在代码中获取**：

```properties
user.name="lijie"
```

使用@Value来获取

```java
@Controller
public void IndexController{
    @Value("${user.name}")
    private String userName;//会把lijie赋值给userName
    
    @RequestMapping(value="/say")
    public @ResponseBody String say(){
        return "hello" + userName;
    }
}
```

**自定义内容映射到对象**：

```properties
user.name="xiaoming"
user.age=11
```

使用@ConfigurationProperties(prefix="xxx")来获取主配置文件中的参数

```java
@Component//这个类交给spring管理
@ConfigurationProperties(prefix="user")
public class User{
    private String name;
    private int age;
    
    public String setName(){...}
    public int setAge(){...}
    public int getAge(){...}
    public String getName(){...}
}
```

## springboot集成jsp

一般在项目的webapp文件夹下放jsp。/main/webapp（需要手动配置）

需要在pom.xml中添加springboot内嵌tomcat对jsp的解析依赖：

```xml
<dependency>
	<groupId>org.apache.tomacat.embed</groupId>
    <aritfactId>tomcat-embed-jasper</aritfactId>
</dependency>

<build>
	<!--springboot默认推荐使用thymeleaf，要集成jsp，需要手动指定jsp编译后的路径，是springboot规定好的位置:META-INF/resources-->
    <resources>
    	<resource>
        	<directory>src/main/webapp</directory>
            <targetPath>META-INF/resources</targetPath>
            <!--指定哪些资源需要编译-->
            <includes>
            	<include>*.*</include>
            </includes>
        </resource>
    </resources>
</build>
```

然后需要在核心配置文件application.yml中配置视图解析器：

```properties
spring.mvc.view.prefix=/
spirng.mvc.view.suffix=.jsp
```

类中返回ModelAndView：

```java
@RequestMapping(value="/say")
public ModelAndView say(){
    ModelAndView mv = new ModelAndView();
    mv.addObject("message","hello");
    mv.setViewName("say");
    return mv;
}

@RequestMappint(value="/index")
public String index(MOdel model){
    model.addAttribute("message","hello");
    return "say";//和上面效果一样
}
```

需要在webapp中创建一个say.jsp.

## 集成Mybatis

依赖：

- 添加mybatis依赖
- 添加mysql驱动

```xml
<dependency>
	<groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
<dependency>
	<groupId>org.mybatis.spring.	boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

**Mybatis逆向工程**：

步骤：

1. 添加mybatis，mysql依赖
2. 添加org.mybatis.generator插件
3. 写一个生成Mapper的配置文件：GeneratorMapper.xml
4. 使用这个插件直接生成。

使用Mybatis提供的逆向工程生成实体bean，映射文件，Dao接口

需要使用一个GeneratorMapper.xml的配置文件：需要配置的一些标签：

- <jdbcConnection>：连接数据库的配置，URL，驱动，用户名和密码
- <javaModelGenerator>：生成model类，targetPackage指定生成的model类的包名，targetProject指定生成的model放在哪个工程下。
- <sqlMapGenerator>：生成mybatis的Mapper.xml文件，targetPackage指定mapper.xml文件的包名，targetProject同理。
- <javaClientGenerator>：生成mybatis的Mapper接口类文件，targetPackage和targetProject同理。
- <table>：数据库表名tableName，以及对应的java模型类名domainObjectName。（如student表，封装成Student类）

mybatis逆向工程只生成单表查询。

mybatis逆向生成的时候，单词之间用下划线隔开，则生成对象的属性会自动驼峰命名。如：字段名user_name->属性名userName。需要在对用的Mapper类上写@Mapper注解。

不在resource目录下的xml配置文件，需要在pom.xml中指定

```xml
<!-- pom.xml -->
<build>
	<resources>
    	<resource>
            <!-- xml配置文件所在位置 -->
        	<directory>xxx</directory>
            <includes>
            	<include>**/*.xml</include>
            </includes>
        </resource>
    </resources>
</build>
```

**注**：如果不使用@Mapper注解，可以再Application.java（入口类）的上面添加@MapperScan()注解，来扫描某个包下所有的类作为Mapper。

**注**：如果不在pom.xml中指定资源文件的位置，可以把需要的配置文件放到resource下，然后再springboot核心配置文件application.properties中指定

```properties
mybatis.mapper-location=classpath:mapper/*.xml
#classpath:mapper/*.xml在项目中的实际位置是：/resource/mapper/mapper.xml
```

maven中规定resource就是classpath，所以可以这么些。然后java文件夹下只会编译其中的.java文件，其它文件不会编译，同时，会编译resource中的内容。所以如果不特别指定，maven不会编译放在java文件夹下的xml文件。

**注解解释**：

- @Mapper：在每一个Mapper接口类上添加，作用是扫描dao接口
- @MapperScan：在SpringBoot入口类上添加，扫描指定的包

**关于Mapper映射文件存放位置的写法**：

- 将Mapper接口和Mapper映射文件放到src/main/java文件夹下，还需再pom.xml中手动指定资源文件夹位置
- 将Mapper接口和Mapper映射文件分开存放，接口类放到java文件夹下，映射文件放到resource类路径下，同时需要在application.properties文件中指定mapper映射文件的位置。

**事务**：

只需要在使用了Mapper中的方法的方法上，使用@Transactional注解就可以开启事务。

