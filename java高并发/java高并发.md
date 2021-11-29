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

**关键字synchronized**：

有多种用法，synchronized所在位置不同，加锁对象不同：

直接指定加锁对象：对给定对象加锁，进入同步代码前要获得给定对象的锁

直接作用于实例方法：相当于对当前实例加锁，进入u同步代码前要获得当前实例的锁

直接作用于静态方法：相当于对当前类加锁，进入同步代码前要获得当前类的锁

**并发不安全的ArrayList**：

比如一致向ArrayList中添加数据，多线程的情况下，可能会出现总数不足，或者报告异常的情况

这似乎是由于在ArrayList扩容的过程中，重复赋值，或者是扩容大小不一致导致。

可以使用线程安全的**vector**代替。

**并发不安全的HashMap**：

除了和ArrayList可能有类似情况以外，在JDK8以前，还有可能出现死循环的情况。

这是由于多线程冲突导致链表结构遭到破坏形成了环。 <span style="color:red">TODO</span>： 为什么！！

**对Integer加锁**：

Integer属于不变对象，也就是无法修改，如果Integer为1，那么需要2就会new一个Integer对象，所以对Integer加锁是没有用的，依然会有并发问题。

### JDK并发包

**可重入锁**：ReentrantLock

就是指可以多次重新获得的锁，获取了多少次，最终也需要释放这么多次。

重要方法有：

lock()：获得锁

unlock()：释放锁

lockInterruptibly()：获得锁，但优先响应中断

tryLock()：尝试 获得锁，成功返回true，失败返回false

tryLock(long time, TimeUnit unit): 在给定时间内尝试获得锁

公平锁：可以给ReentrantLock设置fair属性，代表是否公平，公平的话，会每个等待的线程依次执行。但效率低。

**Condition类**：

使用lock接口的Condition newCondition()方法可以生成一个与当前重入锁绑定的Condition实例，使用这个对象可以执行类似wait和notify的操作：

```java
void await() throws InterruptedException;
void awaituninterruptibly();
void awaitNanos(long nanosTimeout) throws InterruptedException;
boolean await(long time, TimeUnit unit) throws InterruptedException;
boolean awaitUntil(Date deadline) throws InterruptedException;
void signal();
void signalAll();
```

await()和wait()类似，会使得当前线程等待，同时释放当前锁，当其他线程中使用signal()方法或signalAll()方法的时候，线程会重新获得锁并继续执行。

awaitUninterruptibly()方法与await()类似，但并不会在等待过程中响应中断。

singal()用于唤醒一个正在等待的线程。
