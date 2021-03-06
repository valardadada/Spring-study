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

**信号量**：

```java
public Semaphore(int permits);
public Semaphore(int permits, boolean fair);
```

构造方法，permits表示准入个数，fair表示是否公平。

```java
public void acquire();
public void acquireUninterruptibly();
public boolean tryAcquire();
public boolean tryAcquire(long timeout, TimeUnit unit);
public void release();
```

acquire()尝试获得准入许可，失败就等待。tryAcquire()尝试获得，失败返回false，不等待。release()释放一个信号量。

**读写锁**：

允许同时读，但写写操作和读写操作依旧需要互相等待。

**倒计数器：CountDownLatch**：

```java
public CountDownLatch(int count);
//方法：
countDown(); //计数器-1， 倒数一个
await();//等待倒计数器计数完毕
```

当倒数count次之后才能继续执行

**循环栅栏：CyclicBarrier**：

```java
public CyclicBarrier(int parties, Runnable barrierAction);
await(); //计数，并等待计数完毕
```

parties是计数的个数，后面的action是达到计数个数之后会执行的事情。

循环栅栏可以重复技术，计完一轮之后可以再记一轮。

**线程阻塞工具：LockSupport**：

可以再任何位置阻塞线程，并且不会有suspend和resume的问题。

```java
LockSupport.park(); //静态方法，阻塞
LockSupport.unpark(thread); //静态方法，继续执行
```

这里的park和unpark的机制与suspend和resume的机制不同。

每个线程有一个许可，park消费这个许可，unpark使一个许可变得可用。许可不可叠加，只能有一个。总之，是可以unpark先执行，park后执行，也可以让被park的线程继续执行。

**限流**：

限流两种方式：令牌桶和漏桶。

漏桶->整流，固定速率释放消息。

令牌桶->固定速率生成令牌，得到令牌可执行。

RateLimiter就使用了令牌桶的方式。

```java
RateLimiter limiter = RateLimiter.create(n);//创建一个每秒产生n个令牌的令牌桶
limiter.acquire(); //尝试获得一个令牌，如果没有拿到就阻塞
limiter.tryAcquire(); //尝试获得一个令牌，获得返回true，失败返回false，不会阻塞
```

#### JDK线程池

JDK提供一套Executor框架：

```java
//Executor:
execute(Runnable command);
//AbstractExecutorService:
//ThreadPoolExecutor:
//ExecutorService
shutdown();
isShutdown();
isTerminated();
submit();
//Executors:
newfixedThreadPool(int nThreads): ExecutorService //返回一个固定数量的线程池，空闲线程则执行任务，无空闲则进队列
newSingleThreadExecutor(): ExecutorService //返回一个只有一个线程的线程池，多余一个则保存到队列
newCachedThreadPool(): ExecutorService//返回一个根据实际情况扩展的线程池，优先复用空闲线程，队列满后才会创建新的线程
newSingleThreadScheduledExecutor(): ScheduledExecutorService//线程池大小为一的可以定时执行任务的线程池
//ScheduledExecutorService:
schedule(Runnable command, long delay, TimeUnit unit): ScheduledFuture
```

**内部实现**：

```java
public ThreadPoolExecutor(int corePoolSize,  //线程池中线程数量
                         int maximumPoolSize, //最大线程数量
                         long keepAliveTime, //超过corePoolSize的线程存活的时间
                         TimeUnit unit,//存活时间的单位
                         BlockingQueue<Runnable> workQueue,//任务队列，被提交但未执行的任务
                         ThreadFactory threadFactory, //线程工程，用于创建任务
                         RejectedExecutionHandler handler) //拒绝策略，线程太多无法执行时候，如何拒绝
```

**workQueue**：

有四种实现：

直接提交的队列：SynchronousQueue，没有容量，一旦提交就会尝试创建线程

有界的任务队列：ArrayBlockingQueue，新任务未达上线容量时阻塞，达到时尝试创建线程

无界的任务队列：LinkedBlockingQueue，可以无限增加等待任务，会爆内存

优先任务队列：PriorityBlockingQueue，可以有优先级的队列

**拒绝策略**：

略

**自定义线程创建：ThreadFactory**：

可以定义创建线程的时候要做些什么事情。

**扩展线程池**：

ThreadPoolExecutor是可以进行扩展的线程池，提供了beforeExecute()，afterExecute()，terminated()三个接口来对线程池进行控制。构建线程池的时候，重写这些方法就可以获得对应的功能。

**记录异常**：

可以通过继承ThreadPoolExecutor，修改其execute和submit方法，就可以增加对异常的打印。 

**fork/join框架**：

ForkJoinTask任务有两个重要子类：RecursiveAction（无返回值），RecursiveTask（有返回值）

```java
public class CountTask extends RecursiveTask<Long>{
    ...
    public void xxx(){
        CountTask subTask = new CountTask();
        subTask.fork(); //-> 子任务
        subTask.join(); //->主任务等待子任务完成
    }
}
```

**并发容器**：

ConcurrentHashMap：高效的并发HashMap

CopyOnWriteArrayList：写时复制列表。 -> 读不阻塞，读写也不阻塞，写写阻塞，写的时候将原数组复制出来，添加/修改，然后替换回去。

ConcurrentLinkedQueue：使用CAS操作进行同步，没有锁。每两次添加会更新一次tail。同时会有哨兵节点，是由于弹出的时候造成的。

BlockingQueue：一般来说，是在取得时候，如果没有数据，可以阻塞，在放入得时候，如果满了，可以阻塞。放入数据或者取得数据得时候进行相应的notify工作。

ConcurrentSkipListMap：跳表。多层链表，上层链表是下层链表的子集，从上层链表开始扫描，如果上层链表的下一个值大于要查找的值得时候，跳到下一层查找。
