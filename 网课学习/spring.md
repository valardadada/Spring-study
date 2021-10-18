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

   
