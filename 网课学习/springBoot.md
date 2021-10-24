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







