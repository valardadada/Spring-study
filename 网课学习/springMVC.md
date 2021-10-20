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

![image-20211020133855363](C:\myGit\project\spring\网课学习\springMVC.assets\image-20211020133855363.png)

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









