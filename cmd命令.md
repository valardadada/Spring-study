记录一下使用`Spring`中使用到的一些命令：

- 查看进程和使用的端口信息：

  `netstat -ano`：显示端口和对应进程的信息。

  `netsatat -ano | findstr "8080"`：可以快速找到含有字符串`8080`的信息条目。

- 强制关闭进程：

  `taskkill /PID processid -F -T`：强制杀掉进程。`-F`是强制终止，`-T`是终止指定进程极其子进程。