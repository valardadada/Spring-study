# spring

## 概念

Spring是分层的java SE/EE应用全栈轻量级开源框架。以IoC和AoP为核心技术。

耦合：程序间的依赖给关系。包括类的依赖，方法的依赖。

解耦：降低程序的依赖关系。

应该做到：编译器不依赖，运行时才依赖。

思路：利用反射来创建对象，避免new。通过读取配置文件来获得要创建对象的类名。  

bean：可重用组件的含义。

javabean：java语言编写的可重用组件。javabean > 实体类

## IoC

ApplicationContext在创建容器的时候，创建策略默认是立即加载的方式。只要读取完配置文件，就立刻配置文件中的配置对象。比较适合于单例模式。可以设置单例多例，一级是否延迟加载等。

BeanFactory 在创建核心容器的时候，创建对象采取的策略是采用延迟加载的方式。也就是，根据id获取对象的时候，才真正创建对象。比较适合于多例对象。

**作用**：降低程序间的耦合。（依赖关系）。依赖关系交给了spring来管理维护。即，当前类需要用到其他类的对象，由spring提供，只需在配置文件中说明。

**依赖注入**：能注入三类数据。注入不常变化的变量比较合适。

1. 基本类型和String
2. 其它bean
3. 复杂类型，集合
   - <array><value></value>...</array>
   - <list>...</list>
   - <set>...</set>
   - 其它同理

**注**：list结构的：array，list，set。map结构的，map，props。

**注入方法**：

1. 使用构造函数：在<bean>中使用<constructor-arg>标签。这个标签的属性有：
   - type：指定注入的数据的数据类型（指定给谁赋值
   - index：指定注入的数据的位置，索引从0开始（指定给谁赋值
   - name：用于给构造函数中指定名称的参数赋值（指定给谁赋值
   - value：基本类型和String赋值（赋什么值
   - ref：引用赋值（赋什么值
2. 使用set方法：property标签。其属性：
   - name：指定注入时所调用的set方法名称
   - value：略
   - ref：略
3. 使用注解提供

**管理bean的细节**：

1. 创建bean的三种方式：

   - 使用默认构造函数创建：在配置文件中使用bean标签，配id，class，不配其它。
   - 使用普通工厂中的方法创建对象（使用某个类中的方法来创建对象，并存入spring容器）。在<bean>中指定factory-bean和factory-method来获取。
   - 使用静态工厂中的静态方法来创建对象，并存入spring容器。使用class来指定静态工程，然后用factory-method来指定调用静态工程的某个方法来创建需要的对象。
2. bean的作用范围：修改<bean>的scope属性：

   - singleton：单例（默认
   - prototype：多例
   - request：作用于web应用的请求范围
   - session：作用于web应用的会话范围
   - global-session：作用域集群环境的会话范围（全局会话范围）当不是集群环境时，他就是session。
3. bean的生命周期：

   - 单例对象：出生：容器创建时对象创建。活：容器还在，对象一直在。死亡：容器销毁，对象消亡。总结：于容器生命周期一致。
   - 多例对象：出生：按需创建。活：对象使用过程中一直活着（jvm来操作？）。死亡：垃圾回收机制来回收。总结：和普通new的对象差不多。


### 注解方式：

需要在配置文件中指定使用注解，以及注解需要扫描的包。配置需要的标签不在beans的约束中，需要导入context名称空间和约束。

> <context:component-scan base-package="xxx"/>

**四类常用注解**：

- 用于创建对象：作用与在xml中编写一个<bean>标签实现的功能一样

  - @Component：把当前类对象存入spring容器中。属性：value：用于指定bean的id，不写的时候，默认值为当前类名且首字母小写。
  - @Controller：
  - @Service：
  - @Repository：

  他们几个的属性和作用与@Component一样，但是这些注解会让三层结构更清晰.

- 用于注入数据：作用与在xml中的bean标签中写一个<property>标签的作用一样。使用注解的时候，set方法不是必须的了。

  - @Autowired：自动按类型注入，只要容器中有唯一的一个bean对象的类型匹配（其实会，先匹配类型，然后匹配名称），就可以注入成功。可以出现在变量上，或者方法上。没有或有多个就无法注入，会抛出异常。
  - @Qualifier：按类型注入的基础之上，再按名称注入，给类成员注入时不能单独使用，但给方法参数注入的时候可以。（给类的时候要和autowired一起，给方法的时候可以单独）
  - @Resource：按id注入。可单独使用。
  - @Value：注入基本类型和String。还可以使用SpEL。${表达式}。

  注：集合类型只能用XML注入

- 用于改变作用范围：作用与bean中的scope属性实现功能是一样的

  @Scope：指定范围。（Singleton，Prototype等）

- 与生命周期相关的：（了解）作用与bean中的init-method和destroy-method作用一样。

  - @PreDestory：销毁前执行
  - @PostConstruct：构造后执行

新的注解：

- @Configuration：指定当前类是一个配置类（代替xml配置文件）
- @ComponentScan：用于通过注解指定spring在创建容器时需要扫描的包。
- @Bean：将当前方法返回值作为bean对象存入spring的ioc容器。默认id是当前方法的名称。
- @Import：导入其它配置类（传入某个类的字节码xxx.class）
- @PropertySource(xxx.propertites)。指定properties文件的位置。classpath:xxx.propertites，指定类路径下的文件。

**细节**：使用注解配置方法的时候，如果方法有参数，spring框架会去容器中查找有没有可用的bean对象。（@Bean默认用autowired）

这时候需要使用`AnnotationConfigApplicationConetxt`：传入配置类的字节码：

```java
ApplicationContext ac = new AnnotationConfigApplicationContext(SpringConfiguration.class);
```

**细节**：@Configuration注解在类当作AnnotationConfigApplicationContext的参数传入的时候，可以不写。但其它时候，或者有多个的时候，需要写。

**细节**：在方法参数中使用@Qualifier：`public void test(@Qualifier("user01") User user)`。

### AoP

**动态代理**：

- 特点：字节码随用随创建，随用随加载

- 作用：不修改源码的基础上对方法增强

- 分类：

  - 基于接口的动态代理：涉及的类：Proxy。使用Proxy中的newProxyInstance方法创建代理对象。对代理对象的要求：被代理类至少实现一个接口，如果没有则不能用。

    - newProxyInstance方法的参数：

      - ClassLoader：类加载器。用于加载代理对象的字节码，和被代理对象使用同样的类加载器。（user.getClass().getClassLoader()，user为被代理类）

      - Class[]：字节码数组，用于代理对象和被代理对象有相同的方法。（user.getClass().getInterface()）

      - InvocationHandler：用于提供增强的代码，写如何代理。一般写该接口的实现类，通常是匿名内部类。（new InvocationHandler()，执行被代理对象的任何接口方法都会经过该方法）

        ```java
        new InvocationHandler(){
            //proxy->代理对象的引用
            //method->当前执行的方法
            //args->当前执行方法所需的参数
            //返回值->和被代理对象的方法有相同的返回值
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) thorws Trowable{
                //提供增强的代码
                //可以从args获取输入的参数，下标从0开始
          		return method.invoke(producer,args);      
            }
        };
        ```

        

  - 基于子类的动态代理：要求第三方jar包的支持。如cglib。

    - 涉及的类：Enhancer。cglib库提供

    - 创建方法：Enhancer类中的create方法

    - 条件：被代理对象不是最终类

    - 参数

      - Class：被代理对象的字节码
      - CallBack：用于提供增强代码，一般写的是该类的子接口实现类：MethodInterceptor()。被代理对象任何方法会经过该方法。

      ```java
      Enhancer.create(user.getClass(), new  MethodInterceptor(){
         @Override
          public Object intercept(...){}//内容和上面的invoke一模一样
      });
      ```

**概念**：AOP叫做面向切面编程，通过预编译方式和运行期动态代理实现程序功能的统一维护的一种技术。利用AOP可以对业务逻辑的各个部分进行隔离，使得业务逻辑之间的耦合度降低，提高程序的可重用性。

**作用**：在程序运行期间，不对源码进行修改，而对已有方法进行增强。可以减少重复代码，提高开发效率。

**AOP**代理的选择：可以选择代理的方式，基于子类或者基于接口。

**术语**：

- 连接点（Joinpoint）：指被拦截到的点，spring中指的方法，spring只支持方法类型的连接点。

- 切入点（Pointcut）：被增强的连接点就叫切入点。（所有的方法都是连接点，切入点是真正被增强（代理）的点）

- 通知/增强（advice）：拦截到切入点之后需要执行的事情。通知有前置，后置，异常通知和最终通知。环绕通知。

  ```java
  try{
      //前置通知
      xxx;
      method.invoke(xxx,xxx);
      //后置通知
      xxx;
  } catch (Exception e){
      //异常通知
      xxx;
  } finally {
  	//最终通知
      xxx;
  }
  ```

- 引介（Introduction）

- 目标对象（Target）：被代理对象

- 织入（Weaving）：把增强应用到目标对象来创建新对象的过程。Spring采用动态代理织入，AspectJ采用编译器织入和类装载期织入。

- 代理（Proxy）：代理对象

- 切面（Aspect）：切入点和通知的结合。

**基于xml的AOP配置**：

1. 配置通知bean（增强bean）
2. 使用aop:config标签表明开始AOP的配置
3. 使用aop:aspect标签表名配置切面：
   - id属性：切面的标识
   - ref属性：指定通知类bean的id
4. 在aop:aspect标签内部使用对应的标签来配置通知类型
   - aop:before表示前置通知
     - method：指使用增强bean的哪个方法
     - pointcut：指定切入点表达式（格式：关键字execution，访问修饰符 返回值 包名...包名.类名.方法名(参数列表)。`execution(public void com.mytest.service.imp.AccountServiceImpl.saveAccount(int i)) `

**切入点表达式**：表达式的解析需要使用aspectj依赖。

通配：`* \*..\*.*(..)`。

访问修饰符可以省略。

返回值可以使用通配符表示任意返回值。

包名可以使用通配符表示任意包，但有几级包，就需要多少个通配符。

包名可以使用..表示当前包和其子包。

类名和方法名可以使用通配符来实现 通配。

参数列表可以直接写数据类型，可以使用通配符表示任意类型，但必须有参数，使用..表示有无参数，有参数可以任意。

```xml
<aop:config>
    <!-- logger是增强bean -->
	<aop:aspect id="logAdvice" ref="logger">
    	<aop:before method="printlog" pointcut-ref="pt1"></aop:before>
		<!-- 卸载切面内，只能在该切面内用 可写在更外一层，就所有的都可以用了，切点必须在切面前声明 -->
        <aop:pointcut id="pt1" expression=""></aop:pointcut>
    </aop:aspect>
</aop:config>
```

**环绕通知**：在环绕通知中需要显示调用被代理的方法。

使用ProceedingJoinPoint接口，该接口有一个proceed()方法，该方法来调用被代理的方法。

```java
public void aroundPrintlog(ProceedingJoinPoint pjp){
    try{
        //advice
        Object[] args = pjp.getArgs();
        pjp.proceed(args);//显示执行被代理对象的方法
    	//advice
    } catch (Exception e){
        //advice
    } finally{
        //advice
    }
}
```

**基于注解的AOP**：

需要配置文件中写：`<aop:aspectj-autoproxy/>`

基于注解的AOP会导致最终通知在后置和异常通知之前调用。可以使用环绕通知来实现。

- @Aspect：表示当前类是一个增强类

- @Before：前置通知

- @After：最终通知

- @AfterReturning：后置通知

- @AfterThrowing：异常通知

- @Pointcut：表名该方法是一个切点

  ```java
  @Pointcut("execution(* com.mytest.service.impl.*.*(..))")
  private void pt1(){}
  //表示一个表达式为...叫做pt1的切点
  ```

**注**：实践的时候遇到的问题：

```java
public class TestAop {
    @Test
    public void testBefore(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("aoptest.xml");
        IService implService = (IService) ac.getBean("service01");
        implService.saveAccount();
    }
}
```

上面的代码，IService是接口，ImplService是实现类，尽管在声明bean的时候，service01声明为了ImplService类，但这里不能强转为ImplService，只能转为IService接口。这是因为动态代理的时候，会根据接口生成一个新的类，这个类只能cast到接口，不能cast到具体的实现类。

### spring中的jdbcTemplate

用于和数据库进行交互，实现对表的CRUD操作。

spring的内置数据源：`DriverManagerDataSource`。

略。

### Spring中的事务

javaEE体系进行分层开发，事务处理位于业务层，Spring提供了分层设计业务层的事务处理解决方案。

Spring框架为我们提供了一组事务控制的接口。在spring-tx中。

事务控制都是基于AOP的，可以使用编程方式或者配置方式实现。

