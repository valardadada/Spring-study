# java高并发

## java线程基本操作

基本方法：

thread1.start() -> 启动线程

thread1.stop() -> 终止线程，慎用/废弃

thread1.interrupt() -> 中断线程，设置中断标记位

Thread.currentThread() -> 返回当前线程

thread1.isInterruptted() -> 判断是否被中断

thread1.interrupted() -> 判断是否被中断，并清除中断标记

Thread.sleep() -> 线程sleep，如果在sleep期间被中断，会报告一个异常，同时清除中断的标记

object.wait() -> 让获取了这个object锁的线程阻塞，流程是 先synchronized(object)获取对象锁，然后判断条件之后，object.wait()，将当前线程挂载该对象的等待队列中

object.notify() -> 随机唤醒一个该object等待队列上的线程，同样，需要先获取这个object的锁

thread1.suspend() -> 线程挂起，慎用/废弃

thread1.resume() -> 继续执行，和suspend()配对，慎用/废弃

thread1.join() -> 执行这条语句的线程将会等待thread1线程执行完之后再继续执行

thread1.yeild() -> 让出cpu，但也会进入cpu的竞争行列

**值得注意的**：

首先是sleep()，睡眠时候被interrupt()会报告异常并清除中断标记。

然后是wait()和notify()，这两个都需要提前获取对象锁才能使用。

**关键字volatile**：

意思是易变的，这个关键字修饰一个对象之后，虚拟机会更消息的处理这个对象，然后如果这个对象改变，会及时通知其他进程，尽量确保一致性。

**线程组**：

ThreadGroup，使用大概如下

```java
ThreadGroup tg = new ThreadGroup("PrintGroup");
Thread t1 = new Thread(tg, new Thread(){}, "T1");
```

第一行声明一个线程组，第二行将这个线程加入这个线程组。不太清除线程组有什么操作。

**守护线程Daemon**：

发音同demon

```java
Thread t = new DaemonT();
t.setDaemon(true);
t.start();
```

上面是声明一个守护线程并启动的流程，需要在start之前设置true。然后这个会随着启动守护线程的线程一起结束。

**线程优先级**：

线程启动前可以设置优先级，大概是用setPriority()，但是优先级只是修改了概率，并不强制更改执行的可能，这反而带来一些不确定性，我想。

