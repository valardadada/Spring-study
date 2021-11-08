#Docker
## 简介/概念
可以把docker容器想象成是轻量级的虚拟机。
`镜像(image)`：运行的程序+运行环境的'打包'。镜像好比是一个模板，可以通过这个模板来创建**容器服务**，例如：tomcat镜像===>run===>tomcat01容器（提供服务器），通过镜像可以创建多个容器（最终服务/项目运行在容器中）。
`容器(container)`：Docker利用容器技术，独立运行一个或者一组应用，通过镜像来创建。启动，停止，删除...
`仓库(repository)`：存放镜像的地方，类似于git仓库。
## 安装Docker
环境准备：
1.linux基础
2.centos7（例如阿里云服务器）
3.用Xshell连接远程服务器
环境查看：
连接上服务器之后，用一下命令：
uname -r：似乎是查看系统内核版本？
cat /etc/os-release：查看系统版本（centos）
安装：
帮助文档->官网中查看。
阿里云镜像加速：
1.登录阿里云找到容器服务
2.找到镜像加速位置->会有对应的操作提示
3.配置使用
## 运行流程和Docker工作原理简介
以helloworld镜像的运行为例：
1.输入命令运行镜像
2.在本机寻找镜像->如果有，则镜像运行；如果没有，则去远程仓库下载（找不到则返回错误）
相对底层的原理：
Docker是一个c/s结构的系统，Docker的守护进程运行在主机上，通过**socket**从客户端进行访问。
DockerServer接收到Docker-Client的指令，就会执行这个命令！
大致似乎是：客户端->后台**守护进程**—>docker容器，由守护进程来和容器进行交互。
docker比虚拟机快的原因：
1.更少的抽象层次
2.docker利用宿主机的内核，虚拟机需要安装新的guestOS。
## docker基本命令
```
docker version ->版本信息
docker info ->docker系统信息，包括镜像和容器数量
docker cmd --help ->帮助信息
```
***镜像命令***
```
docker images ->查看有哪些镜像
docker search xxx ->搜索镜像
docker pull xxx ->下载镜像，分层下载（可以共用一部分），dockker image核心，联合文件系统？
docker rmi -f containerId ->删除镜像
```
***容器命令***
```
//下载centos
docker pull centos
//新建容器并启动
dockker run [args] image
//参数说明：--name="Name" 容器名， -d 后台方式运行， -it 交互式运行， -p指定容器端口，-P 随机指定端口
//-p的格式 
// -p ip:host prot: container port 
// -p host port : container port
// -p container port 
docker ps ->查看在运行的容器
docker ps -a ->查看所有运行过的容器
ctrl+p+q ->退出但不停止容器
docker rm id ->删除指定容器
docker start id ->启动容器
docker restart id ->重启容器
docker stop id ->停止容器
docker kill id ->杀掉容器
```
***常用其他命令***
```
docker run -d xxx ->后台启动的时候，就必须要有一个前台进程，否则会直接关掉
docker logs ->查看日志
docker top id ->查看进程
docker inspect id ->查看容器元数据
docker exec -it id bashShell ->进入后台容器， 会启动一个新的终端
docker attach id ->进入正在运行的终端
docker cp id:容器内路径 目的主机路径 ->将内容从容器拷贝到本地
docker stats ->查看docker内存占用情况
```
## 可视化
portainer：图形化界面管理工具，提供后台面板供用户操作
```
docker run -d -p 8080:9000 --restart=always -v /var/run/docker.sock:/var/run/docker.sock --privileged=true portainer/portainer
```
Rancher(CI/CD再用)
## Docker镜像原理
镜像是一种轻量级，可执行的独立软件包，用来打包软件运行环境和基本运行环境开发的软件，它包含运行某个软件所需的所有内容，包括代码，运行时，库，环境变量和配置文件。
得到镜像的方式：
1.远程仓库
2.自己制作
### Docker镜像加载原理
> UnionFS(联合文件系统)
联合文件系统（UnionFS）：联合我呢见系统是一种分层，轻量级且高性能的文件系统，它支持对我呢见系统的每一次修改的当作一次提交来层层叠加，同时可以将不同目录**挂载**到同一个虚拟文件系统下（unite several directories into a single virtual file system）。
> 镜像加载原理
docker镜像实际上是由一层一层的文件系统组成，这种层级的文件系统UnionFS。
bootfs（boot file system）主要包含bootloader和kernel，bootloader主要是引导加载内核。
bootfs在docker镜像的最底层，这一层与典型的linux系统一样。
rootfs（root file system）在bootfs之上，包含的就是典型的linux系统中的/dev,/proc,/bin,/etc等标准目录和文件。rootfs就是各种不同操作系统的发行版，如ubuntu，centos等。
### 分层理解
建议直接看视频p19或者百度。
分层的好处是，相同的层可以复用。
docker镜像都是只读的，当容器启动的时候，一个新的可写层被加载到镜像的顶部，这一层就是通常说的容器层，容器之下的叫做镜像层。
### commit镜像
```
docker commit ->提交容器成为一个新的副本
docker commit -m="提交的描述信息" -a="作者" 容器id 目标镜像名:[TAG]
类似于虚拟机的快照。
```
## 容器数据卷
### 什么是容器数据卷
应用和环境打包成镜像 ->但并不希望数据也丢失，需求：数据可以持久化。
所以期望数据可以共享。
卷技术-> docker容器中产生的数据，同步到本地
卷技术：目录的挂载，将容器内的目录，挂载到linux上面。 
总结：容器的持久化和同步操作。容器间也可以数据共享。
### 使用数据卷
> 使用命令 -v
 docker run -it -v 主机目录地址：容器内目录这是**指定路径挂载**。
**具名挂载和匿名挂载**：
匿名挂载：
不指明主机目录地址，会自动将容器内目录挂载到某个位置。
具名挂在：
就是指定卷名：docker run -v 卷名:容器内路径
通过inspect 命令，或者docker volume inspect来查看卷相关的信息。
会存入/var/lib/docker/volumes下。 
**拓展**
修改读写权限：ro ->readonly， rw ->readwrite
docker -d -P --name nginx02 -v juming-nginx:/etc/nginx:ro nginx
## Dockerfile
p23
