# 微服务
测试平台的时候，是不是可以使用jemeter进行压力测试？

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

**Nacos配置管理**：

统一配置管理，配置更改热更新。

将统一的配置（有热更新需求的配置）传递到配置管理服务，从这里读取配置，结合本地配置。

1.在Nacos中添加一个新的配置

配置获取的步骤：

![image-20211114130046453](C:\myGit\project\spring\micro-service\micro-service.assets\image-20211114130046453.png)

2.引入nacos配置管理客户端依赖：

```xml
<dependency>
	<groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

3.在微服务的resource目录添加一个`bootstrap.yml`文件，这个文件是引导文件，优先级高于application.yml：

```yml
spring:
	application:
	  name: userservice #service name
	profiles:
	  active: dev #enviroment
	cloud:
	  nacos: 
	    server-addr: localhost:8848 #nacos address
	    config:
	      file-extension: yaml #suffix
# userservice-dev.yaml->nacos上配置文件的名字
```

**配置热更新**：

需要进行配置：

方式一：在@Value注入的变量所在类上添加注解@RefreshScope

```java
@RefreshScope
public class UserControler{
    
    @Value("${pattern.dateformat}")//注入属性
    private String dateformat;
}
```

方式二：使用@ConfigurationProperties注解

```java
@ConfigurationProperties(prefix = "pattern")// 前缀加变量名一样就可以从配置中载入
public class PatternProperties {
    private String dateformat;
}
```

**多环境配置共享**：

微服务启动的时候会从nacos读取多个配置文件：

- [spring.application.name]-[spring.profiles.active].suffix，例如：userservice-dev.yaml
- [spring.application.name].suffix，例如：userservice.yaml ->通用共享环境

所以可以吧共享配置内容放到第二个配置文件中，不论环境如何改变（开发，测试，发布等）都会读取这个配置。

**多配置的优先级**：

本地配置，远端共享配置，远端环境的配置对相同属性进行配置了之后，会选择哪一个：

服务吗-profile.yaml > 服务名.yaml > 本地配置（最低）

**Nacos集群搭建**：

略。

## http客户端Feign

RestTemplate方式调用存在的问题：

利用RestTemplate发起远程调用的代码：

```java
String url = "http://userservice/user/" + order.getUserId();
User user = restTemplate.getForObject(url, User.class);
```

代码可读性差，编码体验差。

**Feign介绍**：

声明式http客户端，优雅的实现http请求的发送。

**定义和使用feign客户端**;

1.引入依赖：

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
    <artifactid>spring-cloud-starter-openfeign</artifactid>
</dependency>
```

2.在order-service的启动类添加注解开启feign的功能：

```java
@EnableFeignClients
public class OrderApplication{//spring boot 主配置类
    
}
```

3.feign客户端：

```java
@FeignClient("userservice")
public interface UserClient {
    @GetMapping("/user/{id}")
    User findById(@PathVariable("id") Long id);
}
```

基于SpringMVC得注解来声明远程调用得信息。

**自定义feign配置**：

| 类型                | 作用             | 说明                                                     |
| ------------------- | ---------------- | -------------------------------------------------------- |
| feign.Logger.Level  | 修改日志级别     | 四种不同级别：NONE，BASIC，HEADERS，FULL                 |
| feign.coder.Decoder | 响应结果的解析器 | http远程调用的结果做解析，例如解析json字符串作为java对象 |
| feign.codec.Encoder | 请求参数编码     | 将请求参数编码，便于通过http请求发送                     |
| feign.Contract      | 支持的注解格式   | 默认是SpringMVC的注解                                    |
| feign.Retryer       | 失败重试机制     | 请求失败的重试机制，默认是没有，不过会使用Ribbon的重试   |

具体修改方式略。

**Feign的最佳实践**：

方式一：（继承）给消费者的FeignClient和提供者的conroller定义统一的父接口作为标准

![image-20211114152903023](C:\myGit\project\spring\micro-service\micro-service.assets\image-20211114152903023.png)

问题：并不推荐这样做。会造成紧耦合。

方法二：（抽取）：将FeignClient抽取为独立模块，并且把接口有关的POJO，默认的Feign配置都放到这个模块中，提供给所有消费者使用

![image-20211114153053876](C:\myGit\project\spring\micro-service\micro-service.assets\image-20211114153053876.png)

问题：多余，可能某个服务只需要一部分方法，但却需要把所有的方法都引入。

**抽取实践**：

1.首先创建一个module，命名为feign-api，然后引入feign的starter依赖

2.将order-service中编写的UserClient，User，DefaultFeignConfiguration都复制到feigin-api项目中（抽取

3.在order-service中引入feign-api的依赖

4.修改order-service中的所有与上述三个组件有关的import部分，改成导入feign-api中的包

5.重启测试

**注意**：

当feignclient不在SpringBootApplication的扫描包范围内的时候，这些feignclient不能使用，解决方法：

方法一：指定feignclient所在包：

```java
@EnableFeignClients(basePackages = "cn.itcast.feign.clients")
```

方法二：指定FeignClient字节码：

```java
@EnableFeignClients(clients = {UserClient.class})
```

## 网关

![image-20211114155154928](C:\myGit\project\spring\micro-service\micro-service.assets\image-20211114155154928.png)

**搭建网关服务**：

步骤：

1.创建新的module，引入SpringCloudGateway的依赖和nacos的服务发现依赖：

```xml
<!-- gateway dependency -->
<dependency>
	<groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<!-- nacos 服务发现依赖-->
<dependency>
	<groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

2.编写路由配置以及nacos地址

```yml
server:
  port: 10010 # gateway port
spring:
  application: 
    name: gateway # service name
  cloud: 
    nacos:
      server-addr: localhost:8848
    gateway:
      routes: # gateway route setting
        - id: user-service # route id 
          uri: lb://userservice # 路由的目标地址，lb就是负载均衡，后面跟服务名称
          # uri: http://127.0.0.1:8081 固定地址
          predicates: #路由断言， 也就是判断请求是否符合路由规则的条件
            - Path=/user/** # 只要路径以/user开头就可以
        - id: xxx #配置第二个route规则
          uri: xxx
          predicates: 
             - Path=xxx
```

![image-20211114160500698](C:\myGit\project\spring\micro-service\micro-service.assets\image-20211114160500698.png)

**路由断言工厂**：

配置中的断言规则由断言工程读取并处理，转变为对应的路由判断条件。一共提供了11个基本的断言工厂。

**路由过滤器GatewayFilter**：

这是网关提供的过滤去，可以对进入网关的请求和微服务返回的相应做处理。

![image-20211115002449348](micro-service.assets/image-20211115002449348.png)

一共有三十多种过滤工厂。

默认过滤器，会对所有的路由请求都生效。

**全局过滤器**：

全局过滤器的作用也是处理一切进入网关请求和微服务响应，与GatewayFilter作用一样。

但GatewayFilter通过配置定义，处理逻辑是固定的，而GlobalFilter的逻辑需要自己写代码实现。 

定义方式是实现GlobalFilter接口：

```java
public interface GlobalFilter {
    //exchange请求上下文，可以获取请求和响应等信息
    //chain用来把请求委托给下一个过滤器
    Mono<void> filter(ServerWebExchange exchange, GatewayFilterChain chain);
}
```

例如：

```java
@Order(-1)
@Component
public class AuthorizeFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
        MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
        String auth = params.getFirst("authorization");
        if("admin".equals(auth)){
            return chain.filter(exchange);
        }
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}
```

过滤器优先级不同时，数值小的会优先。

当过滤器优先级相同的时候，会按defaultFilter > 路由过滤去 > GlobalFilter的顺序执行。

**跨域问题**：

跨域：域名不一致就是跨域

如：

域名不同：`www.taobao.com`，`www.taobao.org`。

域名相同，端口不同：localhost:8080，localhost:8081。

跨域问题：**浏览器**禁止请求的发起者与服务端发生跨域ajax请求，请求被浏览器拦截的问题。

解决：CORS？？？这是啥

## docker

**镜像和容器**：

镜像：Docker将应用程序及所需的依赖，函数库，环境，配置等文件打包，称为镜像 -> 只读

容器：镜像中的应用程序运行后形成的进程就是容器，知识docker会给容器做隔离，对外不可见。

**docker架构**：

CS架构：

服务端：Docker守护进程，负责处理Docker指令，管理镜像，容器

客户端：通过命令或RestAPI向Docker服务端发送指令。可以在本地或远程向服务端发指令。

**镜像命令**：

镜像一般两部分组成：[repostory]:[tag] 如：mysql:5.7

docker build 构建镜像

docker pull 拉取镜像

docker push 推送镜像

docker save 保存镜像为压缩包

docker load 加载压缩包为镜像

docker images 查看镜像

docker rmi 删除镜像

**容器命令**：

docker run 运行容器

docker pause 容器暂停

docker unpause 容器继续运行

docker stop 容器停止

docker start 容器开始

docker ps 默认查看运行的容器以及状态，可以-a查看所有

docker exec 进入容器内部执行命令

docker logs 容器日志

docker rm 删除指定容器

例子：

```shell
docker run --name containerName -p 80:80 -d nginx
```

解析：

docker run 创建并运行一个容器

--name 容器指定名字

-p 宿主机端口与容器端口映射，冒号左端是宿主机端口，右侧是容器端口

-d 后台运行容器

nginx 镜像的名称，正常是需要带tag，默认latest

例子：

```shell
docker exec -it containerName bash
```

docker exec 进入容器内部，执行一个命令

-it 给当前进入的容器创建一个标准输入、输出终端，允许与容器交互

containerName 容器名

bash 是需要执行的命令

**数据卷命令**：

数据卷：要给虚拟目录，指向宿主机文件系统中的某个目录。

```shell
docker volume [COMMAND]
```

docker volume是数据卷操作命令，命令后跟随的command来确定下一步的操作：

create 创建一个volume

inspect 显示一个或多个volume的信息

ls 列出所有的volume

prune 删除未使用的volume

rm 删除一个或多个指定的volume

挂载实例：

创建容器的时候使用 -v 来挂载，如：-v html: /root/html，将容器的/root/html挂载到html数据卷

没有提前创建这个数据卷的时候，docker也会自动创建一个数据卷。

目录挂载：

-v [宿主机目录]:[容器目录]

-v [宿主机文件]:[容器文件]

**自定义镜像**：

Dockerfile

| 指令       | 说明                                           | 示例                        |
| ---------- | ---------------------------------------------- | --------------------------- |
| FROM       | 指定基础镜像                                   | FROM centos:6               |
| ENV        | 设置环境变量，可在后面指令使用                 | ENV key value               |
| COPY       | 拷贝本地文件到镜像的指定目录                   | COPY ./mysql-5.7.rpm /tmp   |
| RUN        | 执行linux的shell命令，一般是安装过程的命令     | RUN yum install gcc         |
| EXPOSE     | 指定容器运行时监听的端口，是给镜像使用者查看的 | EXPOSE 8080                 |
| ENTRYPOINT | 镜像中应用的启动命令，容器运行时调用           | ENTRYPOINT java -jar xx.jar |

[参考官方网站](https://docs.docker.com/engine/reference/builder).

docker build -t dockerImage:tag location

**DockerCompose**：

Docker Compose可以基于Compose文件帮助我们快速的部署分布式应用，而无需手动一个个创建和运行容器

Compose文件是一个文本文件，通过指令定义集群中的每个容器如何运行。 

例子：

```yaml
version: "3.8"

service:
  mysql:
    image: mysql:5.7.25
    environment:
     MYSQL_ROOT_PASSWORD: 123
    volumes:
     - /tmp/mysql/data:/var/lib/mysql
     - /tmp/mysql/conf/hmy.cnf:/etc/mysql/conf.d/hmy.cnf
  web:
    build: . #从当前目录进行构建
    ports:
     - 8090: 8090
```

## 异步通信技术

### MQ

同步通信和异步通信。

基于Feign的调用就属于同步方式：

代码耦合度高，性能下降，吞吐下降，资源利用率低，级联失败。

异步调用 -> 事件驱动模式，其模式如图：

![image-20211116000444953](micro-service.assets/image-20211116000444953.png) 

异步通信优缺点：

优点：服务解耦；性能提升，吞吐量提高；服务没有强依赖，不担心级联失败的问题；流量削峰；

缺点：broker的可靠性，安全性，吞吐能力要求高；架构复杂，没有明显流程线，不好追踪管理

用于高并发，即时性要求低

**什么是MQ**：

MQ（MessageQueue）：消息队列，存放消息的队列。也就是事件驱动架构中的Broker

主要有四种：

|            | RabbitMQ                | ActiveMQ                          | RocketMQ   | Kafka      |
| ---------- | ----------------------- | --------------------------------- | ---------- | ---------- |
| 公司/社区  | Rabbit                  | Apache                            | 阿里       | Apache     |
| 开发语言   | Erlang                  | Java                              | Java       | Scala&Java |
| 协议支持   | AMQP，XMPP，SMTP，STOMP | OpenWire，STOMP，REST，XMPP，AMQP | 自定义协议 | 自定义协议 |
| 可用性     | 高                      | 一般                              | 高         | 高         |
| 单机吞吐量 | 一般                    | 差                                | 高         | 非常高     |
| 消息延迟   | 微妙级                  | 毫秒级                            | 毫秒级     | 毫秒以内   |
| 消息可靠性 | 高                      | 一般                              | 高         | 一般       |

### RabbitMQ

RabbitMQ是基于Erlang语言开发的开源**消息通信中间件**。

使用Docker 运行MQ容器：

```sh
docker run \
-e RABBITMQ_DEFAULT_USER=itcast \
-e RABBITMQ_DEFAULT_PASS=123321 \
--name mq \
--hostname mq1 \
-p 15672:15672 \
-p 5672:5672 \
-d \
rabbitmq:3-management
```

rabbitMQ结构：

![image-20211116002650612](micro-service.assets/image-20211116002650612.png)

### SpringAMQP

AMQP：Advanced Message Queuing Protocol，是用于在应用程序或之间传递业务消息的开放**标准**。该协议与语言和平台无关，更符合微服务中独立新的要求。

Spring AMQP：是基于AMQP协议定义的一套API规范，提供了模板来发送和接受消息。包含两部分，spring-amqp是基础抽象，spring-rabbit是底层的默认实现。

**实现基础队列功能**：

1.父工程引入spring-amqp依赖

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactid>
</dependency>
```

2.在publisher服务中利用RabbitTemplate发送消息到某个队列

2.1 在publisher服务中编写application.yml，添加mq连接信息

2.2 建造测试类来进行测试

3.在consumer服务中编写消费逻辑，绑定对应队列来消费

3.1 consumer服务中添加mq连接信息

3.2 在这个服务中新建类，编写消费逻辑，使用@RabbitListener注解，来表示监听某个消息队列。

**消息预取机制**：

大量消息到达队列的时候，消息会预先取到消费者那里，而不考虑消费者的能力，最终导致消息被各个消费者平均，而不是根据消费者的能力分。

修改消费者的application.yml，给MQ添加一个prefetch的属性，指定为1，就不能提前取。

**发布订阅模式**：

类似于计算机网络实现的发布订阅的模式，通过路由器（消息队列架构中叫做交换机exchange）来分发给有订阅主机的路由器，最终分发到主机

spring-amqp中有三种常见exchange ->只负责消息的转发（exchange->queue），不负责可靠性，如果消息丢失没法找回。

Fanout：广播，路由给每一个跟其绑定的队列

Direct：路由，将消息根据规则路由到指定的队列

Topic：话题，与direct类似，但是它的routingKey必须是多个单词的列表，以.号分割。 

**FanoutExchange使用**：

1.消费者服务中，用代码声明**队列**，**交换机**，将两者绑定

```java
@Bean
public FanoutExchange fanoutExcange(){
    return new FanoutExchange("fanoutExchange");
}
@Bean
public Queue fanoutQueue1(){
    return new Queue("queue1");
}
@Bean 
public Binding fanoutBinding1(Queue queue1, FanoutExchange fanoutExchange){
    return BindingBuilder.bing(queue1).to(fanoutExchange);
}
```

2.消费者服务中，编写两个消费者方法，分别监听不同的队列

3.在发布者中编写测试方法，向**交换机**发送消息

**DirectExchange**：

通过给队列指定bindingKey，然后在发送消息的时候，需要指定routingKey，routingKey和bindingKey相同的时候，就会发送过去。

同一个队列可以绑定多个bindingKey。

1.利用@RabbitListener注解来声明Exchange，Queue，RoutingKey

```java
@RabbitListener(bindings = @QueueBinding(
    value = @Queue(name = "queue1"), 
    exchange = @Exchange(name = "directExchange1", type = ExchangeTypes.DIRECT),
	key = {"red", "blue"}
))
public void listenDirectQueue1(String msg){
    System.out.println("xxxx");
}//绑定在消费消息的方法上
```

其他消费者和发布者不变。

**TopicExchange**：

可以用通配符来指定绑定的内容。

**消息转换器**：

发送的为Object类型，说明是可以发送任意类型的对象/数据。

java会将对象序列化之后发送java-serialized-object。

Spring对消息对象的处理是由org.springframework.amqp.support.converter.MessageConverter来处理的。而默认实现是**SimpleMessageConverter**，基于JDK的ObjectOutputStream完成序列化。

如果需要**修改**只需要定义一个**MessageConverter**类型的Bean即可。推荐使用json方式序列化，步骤如下：

1.在发布消息的服务中引入依赖：

```xml
<dependency>
	<groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-xml</artifactId>
</dependency>
```

2.在发布消息的服务中声明MessageConverter：

```java
@Bean
public MessageConverter jsonMessageConverter(){
    return new Jackson2JsonMessageConverter();
}
```

3.消息消费者也需要引入jackson依赖

```xml
<dependency>
	<groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-xml</artifactId>
    <version>2.9.10</version>
</dependency>
```

4.在消费者服务定义MessageConverter：和发布消息的服务一样

5.从某个队列中消费消息，转换为对应的数据结构即可

## Elasticsearch

elasticsearch -> 功能非常强打的开源搜索引擎，可以帮助我们从海量的数据中快速找到需要的内容。

组件：kibana，Logstash，Beats也就是elastic stack（ELK）。被广泛应用在日志数据分析，实时监控等领域。

![image-20211116235554476](micro-service.assets/image-20211116235554476.png)

elasticsearch是基于lucene，这是一个java语言的搜索引擎类库，是apache公司的顶级项目，与1999年开发。

优势：易扩展，高性能（基于倒排索引）

缺点：只限于java，学习曲线陡峭，不支持水平扩展

elasticsearch优势：支持分布式，可水平扩展，提供Restful接口，可被任何语言调用。

**正向索引和倒排索引**：

![image-20211117001922975](micro-service.assets/image-20211117001922975.png)

文档：每条数据是一个文档 -> es中会序列化成json格式

词条：文档按照语义分成的词语 -> 词条唯一

索引：相同类型的文档的集合

映射：索引中文档的字段约束信息，类似表的结构约束

倒排 -> 根据词条来找文档。

**创建索引库**：

创建索引库的DSL语法如下：

![image-20211117103129896](micro-service.assets/image-20211117103129896.png)

text类型才需要进行分词，需要指定分词器analyzer，index为是否参与索引，默认为true。

**查询和删除索引库**：

GET /索引库

DELETE /索引库

**修改索引库**：

es中禁止修改索引库->会导致原来的整个倒排索引失效。

允许添加新字段：

PUT /索引库/_mapping

{ 添加的字段内容 }

**添加文档**：

POST /索引库名/_doc/文档id

{

 字段1：值

字段2：值

字段3：值

}

**查询文档**：

GET /索引库名/_doc/文档id

**删除文档**：

DELETE /索引库名/_doc/文档id

**修改文档**：

PUT /索引库名/_doc/文档id   -> 全量新增，如果id对应的文档存在，会完全替代

{ 文档内容 }

POST /索引库名/_update/文档id   -> 局部更新，更新一个字段

{

​	"doc":{

​		字段名：值

​	}

}

### RestClinet

es官方提供了各种不同语言的客户端，来操作ES。这些客户端本质上是组装DSL语句，通过http请求发送给ES。

当想使用多个字段进行搜索的时候，可以在创建索引库的时候使用copy_to将一个字段添加到另外一个字段。

```dsl
"all": {
	"type": "text",
	"analyzer": "ik_max_word"
},
"brand": {
	"type": "keyword",
	"copy_to": "all"
}
```

如上，就可以在搜索的时候，用到所有copy_to指向all的字段。

**初始化JavaRestClient**：

1.引入es的RestHighLevelClient依赖：

```xml
<dependency>
	<groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
</dependency>
```

2.因为springboot默认es版本7.6.2，所以需要覆盖默认的ES版本：

```xml
<properties>
	<java.version>1.8</java.version>
    <elasticsearch.version>7.12.1</elasticsearch.version>
</properties>
```

3.初始化RestHighLevelClient：

```java
RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
	HttpHost.create("http://192.168.150.101:9200")
));
```

**创建索引库**：

```java
void testCreateHotelIndex() throws IOException {
    //1.创建Request对象
    CreateIndexRequest request = new CreateIndexRequest("hotel");
    //2.请求参数，MAPPING_TEMPLATE是静态常量字符串，内容是创建索引库的DSL语句
    request.source(MAPPING_TEMPLATE, XContentType.JSON);
    //3.发起请求
    client.indices().create(request, RequestOptions.DEFAULT);
}
```

**删除索引库，判断索引库是否存在**：

client.indices().delete(xxx,xxx);   -> 删除

client.indices().exists(xxx,xxx);    -> 判断是否存在

总结就是，客户端所有的操作都可以使用client.indices().action()来进行。

**RestClient操作文档**：

添加数据到索引库：

```java
void testIndexDocument() throws IOException {
    //1.创建request对象, indexName -> 索引库名， id -> 文档id
    IndexRequest request = new IndexRequest("indexName").id("1");
    //2.准备JSON文档
    request.source("{\"name\": \"Jack\",\"age\":21}", XContentType.JSON);
    //3.发送请求
    client.index(request, RequestOptions.DEFAULT);
}
```

**查询文档**：

```java
vodi testGetDocumentById throws IOException {
    //1.创建request对象
    GetRequest request = new GetRequest("indexName", "1");
    //2.发送请求，得到结果
    GetResponse respones = client.get(request, RequestOptions.DEFAULT);
    //3.解析结果
    String json = response.getSourceAsString();
    
    System.out.println(json);
}
```

**更新**：

类似上面得，使用UpdateRequest， 然后client.update()。

**删除**：

同理，DeleteRequest， 然后client.delete()。

**文档得CRUD**：

就是client.action()

**批量导入数据到ES**：

```java
void testBulk() throws IOException {
    //1.创建Bulk请求
    BulkRequest request = new BulkRequest();
    //2.添加要批量提交的请求
    request.add(new IndexRequest("hotel").id("101").source("json source", XContentType.JSON));
    request.add(new IndexRequest("hotel").id("102").source("json source2", XContentType.JSON));
    //3.发起bulk请求
    client.bulk(request, RequestOptions.DEFAULT);
}
```

### 分布式搜索引擎

**DSL Query分类**：

DSL（Domain Specific Language）：基于json风格的查询语言。常见查询类型包括：

查询所有：查出所有数据，match_all

全文检索查询：利用分词器对用户输入内容分词，然后去倒排索引库中匹配：例如：match_query，multi_match_query，match

精确查询：根据精确词条值查找数据，一般是查找keyword，数值，日期，boolean类型的字段：ids，range，term

地理查询：根据经纬度查询，geo_distance，geo_bounding_box

复合查询：将上面的各种条件组合起来，合并查询条件，例如：bool，function_score

查询的基本语法：

```dsl
GET /indexName/_search
{
	"query":{
		"查询类型": {
			"查询条件": "条件值"
		}
	}
}
```

如：查询所有

```dsl
GET /hotel/_search
{
	"query"{
		"match_all":{}
	}
}
```

**全文检索查询**：

会对用户输入内容分词，常用于搜索框搜索。

```dsl
# match
"match": {"FIELD": "TEXT"}
#mult_match 不推荐，推荐将多个字段copy_to一个字段，利用哪一个字段进行match
"multi_match": {"query": "TEXT", "fields": ["FIELD1","FIELD2"]}
```

**精确查询**：

一般是keyword，数值，日期，boolean等类型字段。所以不会对搜索内容进行分词。

term：根据词条精确值查询

range：根据值得范围查询

```dsl
"term": {"FIELD":{"value":"VALUE"}}
"range":{"FIELD":{"gte":100,"lte":300}}#gt >, gte >=, lt <, lte <=
```

**地理查询**：

根据经纬度查询

geo_bouding_box：擦汗寻geo_point值落在某个矩形范围得所有文档

```
"geo_bouding_box": {
	"FIELD":{
		"top_left":{
			"lat":31.1,
			"lon":121.5
		},
		"bottom_right":{
			"lat":30.9,
			"lon":121.7
		}
	}
}
```

geo_distance：查询到中心点小于某个距离得所有文档

```
"geo_distance":{
	"distance": "15km",
	"FIELD":"31.21,121.5"
}
```

**复合查询**：

将简单查询组合起来，实现更复杂的搜索逻辑

function_score：算分函数查询，可以控制文档相关性算法，控制文档排名。

词条频率（TF）=词条出现次数/文档中词条总数

（TF-IDF算法）逆文档频率（IDF）=Log（文档总数/包含词条的文档总数），score=所有词条频率求和（TF的和）*IDF

（BM2.5算法）Score(Q,d)=求和log(1+(N-n+0.5)/(n+0.5))\*fi/(fi+k1*(1-b+b\*dl/avg(dl)))

![image-20211118094744595](micro-service.assets/image-20211118094744595.png)

function score query->修改文档得相关性算法，得到新的算分排序

```
"function_score":{
	"query":{"match":{"all":"外滩"}},
	"function":[
		{
			"filter":{"term":{"id":"1"}}, #过滤条件
			"weight": 10 #算分函数，权重作为结果，还有field_value_factor，random_score，
						 #script_score
		}
	],
	"boost_mode":"multiply"#加权模式，两者相乘，还有replace，sum,avg,max,min
}
```

**复合查询Boolean Query**：

布尔查询是一个或多个查询子句的组合，组合方式：

must：必须匹配的子查询 ->算分

should：选择性匹配 ->算分

must_not：必须不匹配 -> 不参与算分

filter：必须匹配，不参与算分   -> 效率高，甚至会缓存

```
"bool":{
	"must":[
		{”term": {"city":"shanghai"}},...#可以有多个子查询
	],
	"should":[
		{"term":{"brand": "NIKE"}}
	],
	...
}
```

**搜索结果排序**：

默认排序，根据相关度算分排序，可以自己指定字段内容来排序，keyword，数值，地理坐标，日期等。

```
"query":{xxx},
"sort":[{"FIELD":"desc"}],
[{"_geo_distance":{"FIELD":"纬度，经度","order":"asc","unit":"km"}}]
```

**搜索结果分页**：

```
"query":{xxx},
"from": 0,#分页开始的位置
"size": 20,#分页的大小
"sort":[{"FIELD":"desc"}],
```

es的倒排索引会将范围内的所有数据排序，之后取分页的内容

深度分页 -> 查询很深的数据

search_after：从上一次的排序值开始，查询下一页的内容

scroll：不推荐使用，排序数据形成快照，放到内存。

**结果高亮**：

突出显示搜索关键字

服务端添加标签，页面中css样式高亮关键字

```
"query":{},
"highlight":{
	"fields":{
		"FIELD":{
			"pre_tags":"<em>",
			"post_tags":"</em>",
			"require_field_match":false #字段名可以不匹配
		}
	}
}
```

默认情况，搜索字段与高亮字段一样才可以高亮

### Restclient查询文档

结果解析：

![image-20211118102546021](micro-service.assets/image-20211118102546021.png)

**查询语句的格式**：

```java
void testMatchAll() throws IOException {
    //1.准备Request
    SearchRequest request = new SearchRequest("hotel");
    //2.准备DSL =====> 不同查询请求，有区别的地方
    request.source().query(QueryBuilders.matchAllQuery());
    //3.发送请求
    SearchResponse response = client.search(request, RequestOptions.DEFAULT);
}
```

match查询：

```java
request.source().query(QueryBuilders.matchQuery("all","test"));
```

term查询：

```java
QueryBuilders.termQuery("xxx","xxx")
```

其他类似。

**搜索结果处理**：

```java
request.source().from(0).size(5);#分页
request.source().sort("price",SortOrder.ASC);#排序
```

高亮

```java
request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));//name是字段名
```

高亮结果解析：

```java
//获取高亮内容：
Map<String, HighlightField> highlightFields = hit.getHighlightFields();
HighlightField highlightField = highlightFields.get("name");
if(highlightField != null){
    String name = highlightField.getFragments()[0].string();
    hotelDoc.setName(name);
}
```

### 数据聚合

聚合：可以实现对文档数据的统计、分析、运算。聚合常见有三类：

参与聚合的字段类型一定不是需要分词处理的字段

桶聚合（Bucket）：用来对文档做分组  -> 对数据做分组

​			TermAggregation：按文档字段值分组

​			Data Histogram： 按日期阶梯分组，如，一周为一组

度量聚合（Metric）：用以计算一些值，如：最大值，最小值，平均值等

​			Avg：平均值

​			Max：最大值

​			Min：求最小值

​			Stats：同时求max，min，avg，sum等

管道聚合（pipeline）：其他聚合的结果为基础做聚合

**DSL实现Bucket聚合**：

如：对品牌名称做聚合

```dsl
GET /hotel/_search
{
	"size": 0, //结果不包含文档，只有聚合结果
	"aggs": { //定义聚合
		"brandAgg":{ //聚合的名称
			"terms":{ //聚合的类型
				"field": "brand", //参与聚合的字段
				"size": 20 //希望获取的聚合结果数量
				"order":{
					"_count": "asc"
				}
			}
		}
	}
}
```

默认对所有文档进行聚合，可以限定聚合的范围 -> 添加query即可。

可以聚合嵌套，如在桶聚合的内部进行metric聚合 ->

```dsl
GET /hotel/_search
{
	"size": 0,
	"aggs":{
		"brandAgg":{
			"terms":{}
			"aggs":{// 进行metric聚合， 在桶聚合的内部
				"scoreAgg":{"stats":xxx}
			}
		}
	}
}
```

### RestClient实现聚合

```java
request.source().size(0);
request.source().aggregation(
	AggregationBuilders
    	.terms("brand_agg")
    	.field("brand")
    	.size(20)
);
```

**结果解析**：

```java
//解析聚合结果
Aggregations aggregations = response.getAggregations();
//根据名称获得聚合结果
Terms brandTerms = aggregations.get("brand_agg");
//获取桶
List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
//遍历
for(Terms.Bucket bucket : buckets){
    //获取key，也就是品牌信息
    String brandName = bucket.getKeyAsString();
    System.out.println(brandName);
}
```

### 自动补全

我觉得可能暂时用不上这些，我想先跳过，大概是p127-p131

### 数据同步

es中的数据来源于数据库，因此数据库数据发生变化的时候，es也必须跟着改变，这就是es与mysql数据库之间的**数据同步**。

**同步的方法**：

同步调用：

![image-20211120000359566](micro-service.assets/image-20211120000359566.png)

类似于写直达，同时写到两个数据库中。

缺点：耦合

异步通知：

![image-20211120000636827](micro-service.assets/image-20211120000636827.png)

业务耦合度降低，复杂度会上升。

监听binlog：

![image-20211120000713070](micro-service.assets/image-20211120000713070.png)

耦合度最低，实现相对复杂，而且会增加mysql的压力。

相对来说，比较推荐方法二 -> 通过消息队列实现异步通知。

### ES集群

跳过了，感觉不太用到，跳到了p143。

## 微服务保护

使用Sentinel来对微服务进行保护

包括流量控制，隔离和降级，授权规则，规则持久化。

### Sentinel

**雪崩问题**：微服务调用中的某个服务故障，引起整个链路中的所有微服务都不可用，这就是雪崩。

解决办法：

超时处理：设定超时时间，请求超过一定时间没有响应就返回错误信息，不会无休止的等待。

舱壁模式：限定每个业务可以使用的线程数，避免耗尽整个tomcat的资源，因此也叫线程隔离。 -> 使用隔离的线程池，线程池用完就不让请求

熔断降级：由**断路器**统计业务执行的异常比例，如果超出阈值会熔断该业务，拦截访问业务的一切请求。 -> 快速释放有问题的服务的请求。

流量控制：限制业务访问的QPS（每秒钟处理请求的数量），避免服务因流量的突增而故障。 -> 预防故障

Sentinel -> 阿里提供的一个微服务流量控制组件。

**sentinel安装和配置**：

```shell
java -jar sentinel-dashboard-1.8.1.jar -Dserver.port=8090
```

使用命令行运行.jar包，提供配置参数就可以修改对应的配置。

需要引入sentinel依赖，以及在配置文件中配置sentinel控制台的地址。

1.引入sentinel依赖

```xml
<denpendency>
	<groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</denpendency>
```

2.配置控制台地址：

```yml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080
```

3.访问微服务的任意端点，触发sentinel监控

**限流**：

簇点链路：项目内的调用链路，链路中**被监控**的每个接口就是一个资源。默认情况下sentinel会监控SpringMVC的每一个端点（Endpoint），因此SpringMVC的每一个端点（Endpoint）就是调用链路中的一个资源。

点击资源/order/{orderId}后面的流控按钮，可以弹出表单。表单中可以添加流量控制规则。

*流控模式*：

三种流控模式：

直接：统计当前资源的请求，触发阈值时对当前资源直接限流，也是默认的模式

关联：统计与当前资源相关的另一个资源，触发阈值时，对当前资源限流

链路：统计从指定链路访问到本资源的请求，触发阈值时，对指定链路限流

关联模式的使用场景：

用户支付时需要修改订单状态，同时用户要查询订单。查询和修改操作会争抢数据库锁，产生竞争。业务需求是有限支付和更新订单的业务，因此当修改订单业务触发阈值时，需要对查询订单业务限流。

Sentinel默认只标记Controller中的方法为资源，如果要标记其他方法，需要利用@SentinelResource注解，示例：

```java
@SentinelResource("goods")
public void queryGoods() {
	System.err.println("query goods");
}
```

Sentinel默认将Controller方法做context整合，导致链路模式的流控失败，需要修改application.yml，添加配置：

```yml
spring:
  cloud:
    sentinel:
      web-context-unify: false #关闭context整合
```

*流控效果*：

流控效果指的是请求达到流控阈值的时候应该采取的措施，包括三种：

快速失败：达到阈值后，新的请求会被立即拒绝并抛出FlowException异常。是默认的处理方式

warm up：预热模式，新的请求会被立即拒绝并抛出FlowException异常。但这种模式阈值会动态变化，从一个较小值逐渐增加到最大阈值。应对服务冷启动的方案，请求阈值初始值是threshold / coldFactor，持续指定时长后，逐渐提高到threshold值。

排队等待：让所有的请求按照先后次序排队执行，两个请求的间隔不能小于指定时长，超时的请求就会被拒绝。

请求进入一个队列中，按照阈值允许的时间间隔依次执行。后来的请求必须等前面的执行完成，如果请求预期的等待时间超出最大时长，则会被拒绝。

*热点参数限流*：

之前的限流是统计访问某个资源的所有请求，判断是否超过QPS阈值，而热点参数限流是分别统计**参数值相同**的请求，判断是否超过QPS阈值。

例如：限制资源的id参数，它可以统计id一样的请求，根据这个统计值来进行限流。

**隔离**：

FeignClinet整合Sentinel。

隔离和熔断都是对**客户端（调用方）**来实现。

实现：

1.修改OrderService的application.yml文件，开启Feign的Sentinel功能。

```yml
feign:
  sentinel:
    enabled: true
```

2.给FeignClient编写失败后的降级逻辑

方式一：FallbackClass，缺点无法对远程调用的异常做处理

方式二：FallbackFactory，可以对远程调用的异常做处理，选择这种

步骤一：feign-api项目中定义类，实现FallbackFactory：

```java
@Slf4j
public class UserClientFallbackFacory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throable throable){
        return new UserClient(){
            @Override
            public user findById(Long id){
				log.error("error", throable);
                return new User();//失败的时候，返回空对象
            }
        }
    }
}
```

步骤二：在默认配置类中将上面的工厂注册为一个bean：

```java
@Bean
public UserClientFallbackFacotry userClientFallback(){
    return new UserClientFallbackFactory();
}
```

步骤三：在feign-api项目中的UserClient接口中使用UserClientFallbackFactory：

```java
@FeignClient(value="userservice", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient{
    @GetMapping("/user/{id}")
    User findById(@PathVariable("id") Long id);
}
```

隔离手段：信号量隔离，线程池隔离。

信号量隔离的特点：基于计数器模式，简单，开销小

基于线程池模式：有额外开销，但隔离控制更强

**熔断降级**：

思路是：由**断路器**统计服务调用的异常比例，慢请求比例，如果超出阈值则会**熔断**该服务，即拦截访问该服务的一切请求；而当服务恢复的时候，断路器会放行访问该服务的请求。

![image-20211122004825632](micro-service.assets/image-20211122004825632.png)

熔断策略有三种：慢调用，异常比例，异常数 

慢调用：业务的响应时长（RT）大于指定时长的请求认定为慢调用请求。在指定时间内，如果请求数量超过设定的最小数量，慢调用比例大于设定的阈值，则触发熔断。

异常比例/异常数：统计指定时间内的调用，如果调用次数超过指定请求数，并且出现异常的比例达到设定的比例阈值（或超过指定异常数），则触发熔断。

**授权规则**：

授权规则可以对调用方的来源做控制，有黑名单和白名单两种方式。

白名单：来源在白名单内的调用者允许访问

黑名单：来源在黑名单内的调用者不允许访问

获取请求来源的接口：RequestOriginParser。

**自定义异常**：

自定义异常时候返回的结果，需要实现BlockExceptionHandler接口：

```java
public interface BlockExceptionHandler {
    void handle(HttpServletRequest request, HttpServletResponse reponse, BlockException e) throws Exception;
}
```

**规则持久化**：

sentinel控制台规则管理由三种模式：

原始模式：sentinel的默认模式，规则保存在内存，重启服务丢失

pull模式：控制台将配置的规则推送到sentinel客户端，客户端会将配置规则保存在本地文件或数据库中。以后会定时去本地文件或数据库中查询，更新本地规则。 -> 时效性差

push模式：控制台将配置规则推送到远程配置中心，例如Nacos。Sentinel客户端监听Nacos，获取配置变更的推送消息，完成本地更新。

## 分布式事务

使用seata来完成。

分布式系统下，一个业务跨越多个服务或数据源，每个服务都是一个分支事务，要保证所有分支事务最终状态一致，这样的事务就是**分布式事务**。

### 理论基础

**cap定理**：

Eric Brewer说，分布式系统无法同时满足下面这三个指标  -> cap定理

Eric Brewer提出，分布式系统三个指标：

一致性：Consistency，用户访问分布式系统中任意节点，得到的数据必须一致

可用性：Availability，用户访问集群中的任意健康节点，必须得到响应，而不是超时或拒绝

分区容错性：Partition tolerance，集群出现分区的时候，整个系统也要持续对外提供服务。

分区：因为网络故障或其他原因导致分布式系统中的部分节点与其他节点失去连接，形成独立分区。

![image-20211122224151109](micro-service.assets/image-20211122224151109.png)

**base理论**：

对CAP的一种解决思路，包括三个思想：

Basically Available（基本可用）：分布式系统在出现故障时，允许损失一部分可用性，即保证核心可用。

Soft State（软状态）：在一定时间内，允许出现中间状态，比如临时的不一致状态。

Eventually Consistent（最终一致性）：虽然无法保证强一致性，但是在软状态结束后，最终达到数据一致。

分布式事务的问题是各个子事物的一致性问题，因此可以借鉴CAP定理和BASE理论：

AP模式：各个子事务分别执行和提交，允许出现结果不一致，然后采用弥补措施恢复数据，实现最终一致。

Cp模式：各个子十五执行后互相等待，同时提交，同时回滚，达成强一致，但在事务等待过程中，属于弱一致。

解决分布式事务，各个子系统之间必须能感知到彼此的事务状态，才能保证状态的一致，因此需要一个事务协调者来协调每一个事物的参与者。

![image-20211122225441878](micro-service.assets/image-20211122225441878.png)

### Seata

阿里和蚂蚁金服提供的开源分布式事务解决方案。

Seata事务管理中有三个重要角色：

TC（Transaction Coordinator） - 事务协调者：维护全局和分支事务的状态，协调全局事务提交或回滚。

TM（Transaction Manager）- 事务管理器：定义全局事物的范围、开始全局事务、提交或回滚全局事务。

RM（Resource Manager） - 资源管理器：管理分支事务处理的资源，与TC交谈以注册分支事务和报告分支事务的状态，并驱动分支事务提交或回滚。

![image-20211122231725101](micro-service.assets/image-20211122231725101.png)

Seata提供四种不同的分布式事务解决方案：

XA模式：强一致性分阶段事务模式，牺牲了一定的可用性，无业务侵入

TCC模式：最终一致性的分阶段事务模式，有业务侵入

AT模式：最终一致的分阶段事务模式，无业务侵入，也是Seata的默认模式

SAGA模式：长事务模式，有业务侵入

**部署TC服务**：

脱离业务的单独服务。

解压，配置（registry.conf）....建议百度，流程漫长。

**微服务集成Seata**：

引入依赖：

```xml
<dependency>
	<groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
    <exclusions>
        <!-- 排除低版本-->
    	<exclusion>
        	<artifactId>seata-spring-boot-starter</artifactId>
            <groupId>io.seata</groupId>
        </exclusion>
    </exclusions>
</dependency>
<!-- seata starter 使用1.4.2版本-->
<dependency>
	<groupId>io.seata</groupId>
    <artifactId>seata-spring-boot-starter</artifactid>
    <version>${seata.version}</version>
</dependency>
```

2.配置application.yml，让微服务通过注册中心找到seata-tc-server：

```yml
seata:
  registr: #Tc服务注册中心的配置，微服务根据这些信息取注册中心获取tc服务的地址
  #参考tc服务自己的registry.conf中的配置
  #包括：地址，namespace，group，application-name， cluster
    type: nacos
    nacos: 
      server-addr: 127.0.0.1:8848
      namespace: ""
      group: DEFAULT_GROUP
      application: seata-tc-server #tc服务在nacos中的服务名称
  tx-service-group: seata-demo #事务组，根据这个获取TC服务的cluster名称
  service:
    vgroup-mapping: #事务组与TC服务cluster的映射关系
      seata-demo: SH
```

**XA模式**：强一致性

XA规范是X/Open组织定义的分布式事务处理（DTP，Distributed Transaction Processing）标准，XA规范描述了全局的TM与局部的RM之间的接口，几乎所有主流数据库都支持XA规范。

一阶段只执行，二阶段等到所有事务都完成了，确定没有问题才提交，如果有一个有问题，就回滚。

缺点，性能差，木桶短板原理。

Seata的starter已经完成XA的自动装配，实现简单：

1.修改application.yml文件（参与事务的微服务），开启XA模式：

```yml
seata:
  data-source-proxy-mode: XA
```

2.给发起全局事务的入口方法添加@GlobalTransactional注解，

```java
@GlobalTansactional
public Long create(Order order){
    orderMapper.insert(order);
    return order.getId();
}
```

**AT模式**：

AT也是分阶段提交的事务模型，但区别是，一阶段执行完各自的就直接提交，但在更新前会形成数据快照，如果失败了，就用快照恢复。

与XA的区别：一阶段提交事务，不锁定资源。回滚机制，利用快照，XA使用数据库回滚。但TA是最终一致，XA是强一致。

AT脏写问题，锁释放了之后，另外的事务可能让这个数据改变。

引入全局锁，由TC记录当前正在操作某行数据的事务，该事务有全局锁，具备执行权，其他事务无法操作这行。

需要修改application.yml，修改事务模式为AT。

**TCC模式**：追求极致的性能

TCC与AT模式很相近，每阶段都是独立事务，不同的是TCC通过人工编码来实现数据恢复。需要实现三个方法：

Try：资源的检测和预留

Confirm：完成资源操作业务；要求Try成功Confirm一定要成功

Cancel：预留资源释放，可以理解为try的反向操作

![image-20211123234728657](micro-service.assets/image-20211123234728657.png)

二阶段始终在操作冻结的部分，不会影响真正可用的部分。 

不依赖于数据库事务，而是依赖补偿操作，可以用于非事务型数据库。

缺点：代码侵入，需要人为编写try，Confirm和Cancel接口；软状态，事务是最终一致；需要考虑Confirm和Cancel的失败情况，做好幂等处理。

需要在服务代码中添加try，confirm和cancel逻辑

分布式事务中各个微服务可以使用自己独立的分布式事务模式。

*空回滚*：当某个分支事务的try阶段阻塞的时候，可能导致全局事务超时而触发二阶段的cancel操作。但未执行try操作时先执行了cancel操作，这时的cancel不能回滚，就是*空回滚*。

*业务悬挂*：对于已经空回滚的业务，如果以后继续执行try，就永远不可能confirm或cancel，这就是*业务悬挂*。应当阻止执行空回滚之后的try操作，避免悬挂。

**saga模式**：

saga模式是seata提供的长事务解决方案，也分两个阶段：

一阶段：直接提交本地事务

二阶段：成功则什么都不做，失败则通过编写补偿业务来回滚

![image-20211124002002816](micro-service.assets/image-20211124002002816.png)

## Redis

单点redis的问题：

使用内存，快，但服务重启可能导致数据丢失。-> redis数据持久化

并发能力问题，单节点并发能力不错，但无法应对高并发场景。 -> 搭建主从集群，实现读写分离

故障恢复问题，redis宕机，则服务不可用，需要一种自动的故障恢复手段。 -> redis哨兵机制，实现健康检测和自动恢复。

存储能力问题，基于内存，单节点存储的数据量难以满足海量数据的存储需求。 -> 搭建分片集群，利用插槽机制实现动态扩容。

### Redis持久化

**RDB**

RDB：Redis Database Backup file（redis数据备份文件），也叫做redis数据快照。简单来说就是把内存中的所有数据都记录到磁盘中。当redis实例故障重启后，从磁盘读取快照文件，恢复数据。

快照成为RDB文件，默认是保存在当前运行目录。

使用save命令，会阻塞进程。bgsave会开启子进程执行RDB，避免主进程受到影响。

redis.conf中可以配置RDB自动触发的机制。

bgsave开始时会fork主进程得到子进程，子进程**共享**主进程的内存数据。完成fork之后读取内存数据并写入RDB文件。

![image-20211124102426169](micro-service.assets/image-20211124102426169.png)

fork会进行copy-on-write技术：

主进程执行读写操作的时候，访问共享内存

主进程执行读写操作时，则会拷贝一份数据，执行写操作

bgsave流程：

fork得到子进程，共享内存空间

子进程读取内存数据写入rdb文件

用新的rdb替换旧的rdb文件

**AOF**：

AOF，Append Only File（追加文件）。redis处理的每一个读写命令都会记录在AOF文件，可以看作是命令日志文件。

修改redis.conf来开启AOF，appendonly yes。

解决AOF文件过大，且记录对同一个key的多次写操作，但只有最后一次写操作才有意义。通过执行bgrewriteaof命令，让aof文件执行重写功能，用最少的命令达到相同的效果。
