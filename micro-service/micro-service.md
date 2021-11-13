# 微服务
## 业务拆分
将不同业务拆分，要求业务独立，不重复实现功能
## 微服务远程调用
例子：使用SpringCloud提供的RestTemplate
通过http请求来完成相互之间的通信。

## Eureka

服务注册

1.引入依赖：

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

2.启动类，添加@EnableEurekaServer注解

3.在application.yml中添加Eureka的配置

注册客户端依赖：

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

需要在application.yml中添加配置。

### 服务拉取

修改某个服务的代码，修改访问url的路径，用服务名代替ip和端口：

```java
String url = "http://userservice/user/" + order.getUserId();
```

在服务项目的启动类中的RestTemplate添加负载均衡的注解：

```java
@Bean
@LoadBalanced
public RestTemplate restTemplate(){
    return new RestTemplate();
}
```

## Ribbon负载均衡

Eureka的负载均衡由Ribbon组件完成。

http请求被Ribbon拦截，然后Ribbon向Eureka拉取服务列表，然后根据设定的规则进行服务器的选择。

![image-20211112162803325](C:\myGit\project\spring\micro-service\micro-service.assets\image-20211112162803325.png)

负载均衡的配置：

1.全局，代码：

```java
@Bean
public IRule randomRule(){
    return new RandomRule();
}
```

2.配置文件：在对应服务的application.yml中，添加新的配置/修改规则：->针对某个微服务

```yml
userservice:
	ribbon:
		NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
```

## Nacos

服务注册，比Eureka功能更丰富。

服务注册到Nacos：

1.在cloud-demo父工程中添加spring-cloud-alilbaba的管理依赖：

```xml
<dependency>
	<groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-dependencies</artifactId>
    <version>2.2.5.RELEASE</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

2.注释掉order-service和user-service中原有的eureka依赖

3.添加nacos的客户端依赖：

```xml
<dependency>
	<groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

4.修改user-service和order-service中的application.yml文件，注释eureka地址，添加nacos地址

```yml
spring:
	cloud:
		nacos:
			server-addr: localhost:8848
```

5.启动并测试

**nacos服务分级存储模型**：

一个服务可以有多个实例。

![image-20211113160832158](C:\myGit\project\spring\micro-service\micro-service.assets\image-20211113160832158.png)

可以在application.yml中设置集群的属性cluster-name。

**nacos环境隔离**：

namespace来做环境隔离，可以在服务的配置文件中配置到不同的环境。

不同空间/环境的不能互相访问。

**临时实例**：

临时实例需要给nacos注册中心发心跳包。

非临时实例会被nacos主动询问，即便非临时实例down了，也不会删除，只会标记不健康。

