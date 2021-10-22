# SpringMVC

## 概念

MVC设计模型

- M：model->模型，javaBean对象
- V：view->视图，JSP
- C：controller->控制器，Servlet

SpringMVC是基于java实现的MVC设计模型的请求驱动类型的轻量级Web框架，属于Spring Framework的后续产品。

**SpringMVC和Struts2的异同**：

同：都是表现层框架，基于MVC模型。底层都离不开servletAPI。处理请求的机制都是一个核心容器。

异：

1. SpringMVC入口是Servlet，而Struts2是Filter。
2. SpringMVC是基于方法设计的，后者基于类，后者每次执行都会创建一个动作类。所以SpringMVC会稍微快一点。
3. SpringMVC更简洁，且支持JSR303，处理ajax请求更方便。Struts2的OGNL表达式的页面开发效率更高一点，但执行效率并没有比JSTL提升。

步骤：

1. 启动服务器，加载配置文件
   - DispatcherServlet对象创建（web.xml配置文件
   - springmvc.xml加载（Spring配置文件
   - HelloController成为容器中的对象
   - internalResourceResolver注册成为容器中的对象
2. 发送请求，后台处理请求

![image-20211020133855363](G:\myGit\project\spring\网课学习\springMVC.assets\image-20211020133855363.png)

**注解**：

- @RequestMapping：可以用在类上和方法上，用在类上表示一级地址，用在方法是表示二级，例如/user/hello。

  属性：

  - name：
  - path（等同value属性）：用于指定请求的URL。
  - method：指定接收什么请求方法（GET，POST等）
  - params：指定请求的时候必须传递的参数，如果不传这个参数就不行
  - headers：指定必须包含的请求头 

**数据绑定**：

绑定机制：

1. 表单提交的数据是k=v格式的，username=haha
2. SpringMVC的参数绑定过程是把表单提交的请求参数，作为控制器中方法的参数进行绑定的
3. 要求：提交表单的name和参数名相同

支持的数据类型：

1. 基本数据类型和字符串类型
2. 实体类型（JavaBean
3. 集合数据类型（List，Map等

基本数据类型和字符串类型：

1. 提交表单的name和参数的名称相同的
2. 区分大小写

实体类型（javaBean）

1. 提交表单的name和javaBean中的属性名称需要一致
2. 如果一个javaBean类中包含其它的引用类型，那么表单的name属性需要写成：对象.属性 例如：address.name

集合属性数据封装

1. JSP页面编写方式：list[0].属性

**自定义类型转化器**：

步骤：

1. 定义一个类，实现Convert接口，该接口有两个泛型
2. 在spring的配置文件中配置自定义的转换器，<bean id="convert" class="org.springframework.context.support.ConversionServiceFactoryBean">。需要注入converters属性，把自己写的自定义转换器加入进去。
3. 配置类型转换器生效。<mvc:annotation-driven conversion-service="conversionService"/>

### 常用注解解释：

@RequestParam：获取请求中的参数，映射过来，但好像没有解释多个参数的情况。

```java
@Controller
@RequestMapping("/anno")
public class AnnoController{
    @RequestMapping("/testRequestParam")
    public String testRequestParam(@RequestParam(name="name")String username){
        //这里的参数使用的叫做name，但如果请求中的参数名字不同，就需要加@RequestParam注解，如上注解后，请求中必须带name参数，否则会400.
        System.out.pringln("executing")；
            return "success";
    }
}
```

@RequestBody：获得请求体内容，得到key=value&key=value...结构的数据。get请求不适用（没有请求体，得写表单才可以）（get请求得参数在地址栏中）。

```java
@RequestMapping("/testRequestBody")
public String testRequestBody(@RequestBody String body){
    System.out.println("executing");
    System.out.println(body);
    return "success";
}
```

@PathVaribale：用于绑定url中得占位符。例如：请求url中得/delete/{id}，这个{id}就是url占位符，这是springMVC支持rest风格URL得一个重要标志。

```java
@RequestMapping("/testPathVariable/{sid}")
public String testPathVariable(@PathVariable(name="sid")String id){
    //请求：anno/testPathVariable/10 -> 获得10
    System.out.println("executing");
    System.out.println(id);
    return "success";
}
```

@RequestHeader：用于获取消息头

```java
@RequestMapping("/testRequestHeader")
public String testPathVariable(@RequestHeader(value="Accept")String header){
    //请求：anno/testPathVariable/10 -> 获得10
    System.out.println("executing");
    System.out.println(header);
    return "success";
}
```

@CookieValue：用于获取指定cookie的名称的值

```java
@RequestMapping("/testCookie")
public String testPathVariable(@CookieValue(value="JSESSIONID")String cookie)
    //获取cookie中的JSESSIONID域
```

@ModelAttribute：修饰方法和参数。放在方法上，表示当前方法会在控制器的方法执行之前，先执行。可以修饰没有返回值的方法，也可以修饰有具体返回值的方法。////用在参数上，获取指定的数据给参数赋值。

```java
@RequestMapping("/testModelAttribute")
public String testPathVariable()
    
@ModelAttribute
public void showUser(){}
//showUser方法会在上面一个方法之前执行
//可以通过@ModelAttribute注解的方法来对传过来的参数进行预处理
```

@SessionAttribute：用于多次执行控制器方法间的参数共享。

### Response响应

返回字符串的方式进行响应：

```java
@Controller
@RequestMapping("/user")
public class UserController{
    
    @RequestMapping("/testString")
    public String testString(Model model){
        System.out.println("testString excute");
        model.addAttribute("user",new User());//可以在jsp中通过${user}来访问这个对象
        return "success";//根据试图解析器跳转到叫做success.xxx的页面去
    }
}
```

无返回值的方式：

```java
    @RequestMapping("/testVoid")
    public String testVoid(){
        System.out.println("testString excute");
    }//这种情况会自动访问对应文件夹下的testVoid.jsp
	@RequestMapping("/testVoid")
    public String testVoid(HttpServletRequest request, HttpServletResponese response) throws ServletException, IOExcepteion{
        System.out.println("testString excute");
        request.getRequestDispatcher("/WEB-INF/pages/success.jsp").forward(request,response);
        //手动转发的时候不会调用视图解析器，所以需要写完整路径。
    }
```











