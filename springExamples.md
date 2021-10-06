# Spring Quick Start Examples

## 1. Building a RESTful Web Service

有一个资源代表类->普通的`java`类。来代表资源。

资源控制器：在`Spring`里面，控制器被用来处理`HTTP`请求。使用`@RestController`注解标识。

**控制器代码示例解析**：

```java
@GetMapping("/greeting")
public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
	return new Greeting(counter.incrementAndGet(), String.format(template, name));
}
```

有一下的一些注意的内容：

- `@GetMapping`注解确保`HTTP`传到`/greeting`的`GET`请求被映射到`greeting()`方法。

  类似的还有`@PostMapping`注解等。还有通用的`@RequestMapping`注解，可以通过设置来处理任何请求，如：`@RequestMapping(method=GET)`。

- `@RequestParam`把查询字符串`name`的值绑定到`greeting()`方法的`name`参数，并设置了默认值（`World`）。

  传统`MVC`控制器和`RESTful web service`控制器的区别是创建`HTTP`响应体的方式。`RESTful`控制器产生并返回一个`Greeting`对象，而不是依赖于依赖视图技术将`greeting`的数据传送给`HTML`。这个对象会以`JSON`的格式直接写到`HTTP`里面。

- 这段代码使用了`@RestController`注解，它标记了这个类作为控制器，它的每个方法都返回一个领域对象而非一个视图。它是`@Controller`和`@ResponseBody`的合并缩写。
- `Greeting`对象必须能够转换到`JSON`。`Spring`的`MappingJackson2HttpMessageConverter`会自动进行转换。
- `@SpringBootApplication`是一个很方便的注解，它添加了如下内容：
  - `@Configuration`：为应用上下文容器标记这个类作为`bean`定义的源代码。
  - `@EnableAutoConfiguration`：告知`Spring Boot`开始添加基于类路径的设置，其他的`bean`和各种属性设置。例如，`spring-webmvc`在类路径上，这个注解会标记应用为`web`应用，并且激活关键行为，例如设置`DispatcherServlet`。
  - `@ComponentScan`：告知`Spring`在`com/example`包中寻找其它容器，配置和服务，让他找到控制器。

`main()`方法中使用了`Spring Boot`的`SpringApplication.run()`方法来启动一个应用。这里没有任何一行`xml`，也没有`web.xml`文件。这个应用是`100%`纯`java`，并且你无需处理任何配置，插件或者基础结构？。

使用`maven`的时候，可以使用`mvn clean package`来打包，使用`java -jar target/gs-rest-service-0.1.0.jar`来运行。或者进入到`pom.xml`文件路径下，执行`mvn spring-boot:run`来运行。

## 2. Consuming a RESTful Web Service

首先有一个`domain`类->包含了需要的数据。其中每个属性都有对应的`getter`和`setter`。

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {...}
```

- 使用了一个`@JsonIgnorProperties(ignoreUnknown = true)`：这段的意思是让`JSON`的处理库忽略任何没有绑定的类型。

  绑定的方式是，指定变量的名字和从`JSON`文档返回的内容的`key`值一样。以防不匹配，可以使用`@JsonProperty`来显示指定。

还有一个嵌入引用本身的类->`value`，大概是`JSON`文档返回的内容中的第二个混合数据的类。编码上没有什么特别的。

然后是一个有`main()`方法的类，作为入口，用来展示获取到的内容，还需要在里面有：

- `logger`记录器：把输出发送到`log`。
- `RestTemplate`模板？：使用`JSON`的`processing`库来处理收到的数据。
- `CommindLineRunner`命令行运行器：用来运行`RestTemplate`。

这个类没有给更多的解释了，但是里面的一些注解可以了解：

- `@Bean`：用在方法上面，表示这个方法会产生一个需要`Spring`容器管理的`bean`。

  ```java
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
  	return builder.build();
  }
  ```

这很奇怪，我感觉里面的`Quote`类，`Value`类都不关键，关键的是`Application`这个类，但是他又没有仔细讲：

```java
@SpringBootApplication
public class ConsumingRestApplication {

	private static final Logger log = LoggerFactory.getLogger(ConsumingRestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ConsumingRestApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {
			Quote quote = restTemplate.getForObject(
					"https://quoters.apps.pcfone.io/api/random", Quote.class);
			log.info(quote.toString());
		};
	}
}
```

这是我觉得关键的类的一部分内容（没有`import`），里面的`logger`的设置，`CommindLineRunner`这些都没有讲。。。

另外，其实学到这里，大概就可以整一个一边客户端一边服务器的简单应用了？

## 3. Building Java Projects with Maven

首先是创建如下的目录结构：

```java
└── src
    └── main
        └── java
            └── hello
```

在`hello`下面创建对应的类。在根目录创建一个`pom.xml`：

- `<modelVersion>`：`POM`模型的版本。
- `<groupId>`：项目属于的组或者组织。
- `<aritfactId>`：项目的库文件的名称（似乎是打包成`jar`的名称）
- `<version>`：项目`build`时候的版本。
- `<packaging>`：项目如何打包。默认`jar`，可选`war`。

命令：

- `mvn compile`：执行编译目标，会在`target/class`文件夹下看到编译的`.class`文件。
- `mvn package`：会编译，运行任何测试对你的代码，然后打包成对应的文件，文件名会基于`<artifactId>`和`<version>`。
- `java -jar xxx.jar`：执行`jar`包。
- `mvn install`：安装你的应用需要的所有依赖。

**在`pom.xml`中声明依赖**：

假设你有一个程序有这样的依赖：

```java
import org.joda.time.LocalTime;
```

那么在`pom.xml`文件中，你应该声明依赖（在`<project>`元素之中）：

```xml
<dependencies>
		<dependency>
			<groupId>joda-time</groupId>
			<artifactId>joda-time</artifactId>
			<version>2.9.2</version>
		</dependency>
</dependencies>
```

依赖由三个部分定义：

- `<groupId>`：依赖所属的组或者组织。
- `<artifactId>`：要求的库。
- `<version>`：库的版本。

默认所有的依赖的范围都是编译依赖项。即，在编译时就可获得这些依赖。可以使用`<scope>`来指定依赖的范围。

**写一个测试**：

首先在`pom.xml`中添加`JUnit`依赖。

然后写一个测试样例：有`@Test`注解。

`Maven`使用`surefire`来运行`unit`测试，默认会运行`src/test/java`里面所有`*Test`的文件。可以使用`mvn test`来测试。

