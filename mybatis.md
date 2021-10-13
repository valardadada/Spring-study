# Mybatis

## 概念

三层结构：

- 表现层：数据展示（SpringMVC框架）
- 业务层：业务逻辑
- 持久层：和数据库交互（Mybatis框架）

持久层技术：

- JDBC技术（规范）：
  - Connection
  - PreparedStatement
  - ResultSet
- Spring的JdbcTemplate（工具类）：
  - Spring中对jdbc的简单封装
- Apache的DBUtils（工具类）：
  - 略。

**MyBatis**：通过XML或者注解的方式将要执行的各种statement配置起来，通过java对象和statement中的sql的动态参数进行映射生成最终执行的sql语句，最后由mybatis框架执行sql并将结果映射为java对象并返回。

**ORM**：Object Relational Mapping对象关系映射，把DB表中的数据和java的实体类的属性对应起来。

实体类中的属性和数据库表中的字段名保持一直。

MyBatis环境搭建

1. 创建maven工程，并导入mybatis坐标（依赖）。
2. 创建实体类和dao接口
3. 创建mybatis的主配置文件SqlConfig.xml
4. 创建映射文件IUserDao.xml

注意事项：

- 创建IUserDao.xml和IUserDao.java的时候，名称要保持一致。MyBatis称持久层的操作接口名称和映射文件为Mapper。IUserDao和IUserMapper是一样的意思。
- mybatis映射的配置文件位置必须和dao接口的包结构相同。（也就是com.mytest.dao.IUserDao.java对应resource/com.mytest/dao/IUserDao.xml）。
- 映射配置文件的mapper标签的namespace属性取值必须是dao接口的权限定类名。
- 映射配置文件的操作配置（select），id属性取值必须是dao接口的方法名。

遵守了后面这几点就可以不实现Dao的实现类。如果不遵守就需要。

---

根据`《SSM从零开始学》`这本书的描述，上面的这部分有如下详细的描述：

从头来说，这里应该是两种方式：

一种是写一个Dao（或者叫Mapper）接口实现类，称为**传统Dao方法**，最终通过调用Dao实现类中的方法来进行CRUD的操作。

另一种叫做**Mapper接口方法**，这个方法不需要写Dao接口的实现类，会使用代理的方式来增强接口的方法，只需要有和接口对应的一个配置文件，这个配置文件需要满足以下一些条件：

1. Mapper接口的名称和对应的Mapper.xml映射文件名必须一样（就是配置文件名和接口类名必须一样）
2. Mapper.xml文件中的namespace和Mapper接口的类路径相同（即Mapper.xml中指定namespace的时候，必须使用接口的全限定类名）
3. Mapper接口中的方法名和Mapper.xml中定义的每个执行语句的id相同。
4. Mapper接口中的方法的输入参数类型必须和Mapper.xml中对应的SQL的parameterType相同。（输入参数类型一致
5. Mapper接口中的方法的输出参数类型必须和Mapper.xml中对应的SQL的resultType相同。（返回类型一致

满足上面的这些规范，MaBatis就可以自动生成Mapper接口实现类的代理对象。

---

读取资源文件的方式：后两种更好。

- 绝对路径：和机器相关，换个机器可能就不对了。
- 相对路径：当发布成web应用的时候，也会不对。
- 使用类加载器：可以，但只能读取类路径的配置文件。
- 使用ServletContext对象的getRealPath()。可以。

**mybatis使用构建者模式来创建工厂**：`new SqlSessionFactoryBuilder.build(configIn)`。

优势：隐藏对象的创建细节。

**使用工厂模式来创建SqlSession**：`factory.openSession()`。	

优势：解耦，降低类之间的关系。

**使用代理模式来创建Dao接口实现类**：`session.getMapper(IUserDao.class)`。

优势：不修改源码的基础上对方法增强。（不写实现类，但对方法进行增强。）

## Mybatis参数

传递pojo对象，Mybatis使用ognl表达式解析对象字段的值，#{}或者${}括号中的值为pojo属性名称。

**OGNL表达式**：Object Graphic Navigation Language：对象图导航语言。通过对象的取值方法来获取数据，写法上省略了get。

例：类写法：`user.getUsername()`，OGNL写法：`user.username`。能这么写的原因是parameterType中提供了属性所属的类，所以无需对象名。

## 结果类型封装

如查询结果的列名和实体类的属性名不对应的时候需要设置映射：

```xml
<resultMap id="userMap" type="com.itheima.domain.User">
	<id property="userId" column="id"></id><!-- 主键-->
    <result property="userName" column="name"></result>
    ...
</resultMap><!--最后在sql语句的位置，不使用resultType，使用resultMap="userMap"-->
```

除了使用resultMap，还可以使用在查询的时候使用别名（命名为实体类中的属性名）

### Mybatis连接池

MyBatis连接池提供三种配置类型：

- 主配置文件SqlMapConfig.xml中的dataSource标签，type表示连接池的方式。有如下几类type：
  - POOLED：传统javax.sql.DataSource规范中的连接池，mybatis中有针对 规范的实现**从池中获取连接来使用**。
  - UNPOOLED：传统获取连接的方式，虽然也实现了DataSource的接口，但并没有使用连接池的思想**每次都新建一个连接**。
  - JNDI：使用服务器的提供JNDI技术实现，来获取DataSource对象。...

## Mybatis事务

事务：通过sqlsession对象的commit和rollback方法实现事务的提交和回滚，最终会调用到java.sql.conection类的commit方法。

事务的ACID：

不考虑隔离会产生的3个问题：

解决办法：
