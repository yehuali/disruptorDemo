### Unsafe类介绍
- 参考资料：http://www.cnblogs.com/mickole/articles/3757278.html
- 1.Unsafe类提供了硬件级别的原子操作
  - 可以分配内存，可以释放内存
  - 可以定位对象某字段的内存位置，也可以修改对象的字段值，即使是私有的
       - 字段定位：staticFieldOffset  --->返回给定field的内存地址偏移量固定不变
       - getInVolatile：获取对象offset偏移地址对应的整型field的值，支持volatitle load语义
       - getLong
  - 挂起与恢复 （参考LockSupport）
       - 挂起：park  线程将一直阻塞直到超时或者中断等条件出现
       - 恢复：unpark    
  - CAS操作
       -  compareAndSwapXXX
       

### 内存屏障介绍
- 参考资料：http://ifeve.com/disruptor/ <br/>
          http://ifeve.com/disruptor-memory-barrier/
- 是一个CPU指令
  - 确保一些特定操作执行的顺序
  - 影响一些数据的可见性（可能是某些指令执行后的结果）
    - 编译器和CPU可以保证输出结果一样的情况下对指令重排序，使性能得到优化
  - 强制更新一次不同CPU的缓存
    - 一个写屏障会把这个屏障前写入的数据刷新到缓存
         - 任何试图读取该数据的线程将得到最新值
 
- 和JAVA关系
 - 如果字段是volatile，JAVA内存模型将在写操作后插入一个写屏障指令，在读操作钱插入一个读屏障指令          
    - 写入完成后，任何访问这个字段的线程将会得到最新值 <br/>
       写入前，保证之前所有发生的事已经发生，并且任何更新过的数据值也是可见的    
 
- 在Disruptor的应用
 - RingBuffer的指针(cursor):是一个volatile变量
   - 生产者将会取得下一个Entry，并对它作任意改动
   - 改动完成后，生成者对ring buffer调用commit方法更新序列号(把cusor更新为该Entry的序列号)
 - 消费者中序列号是volatile类型的，会被若干个外部对象读取（其他下游消费者可能在跟踪这个消费者）
   - ProducerBarrier/RingBuffer跟踪它以确保没有出现重叠的情况
 
- 对性能影响
 - 内存屏障作为CPU级的指令，没有锁那样大的开销
   - 弊端：（1）不能重排序，导致没有高效利用CPU <br>
           （2）刷新缓存也有开销
 - Disruptor的实现对序列号的读写频率尽量降到最低
   - 做法:获取一整批Entries，并在更新序列号前处理它们<br>
          --->使用局部变量来递增，减少对volatile类型的序列号的进行读写<br>
          

### LAMX架构          
1. 简介
    - 核心：业务逻辑处理器
        - 完全运行在内存中
        - 使用事件源驱动方式  
        - 业务逻辑处理器核心是Disruptors（是一个并发组件）
   
2. 整体架构
![LAMX整体架构](http://ifeve.com/wp-content/uploads/2013/01/arch-summary.png)
    - 业务逻辑处理器处理所有的应用程序的业务逻辑
        - 是一个单线程的Java程序，不需要任何平台框架，运行在JVM里
  
3. 业务逻辑处理器
    - 全部驻留在内存中
        - 整个操作在内存中，没有数据库或者其他持久存储
        - 缺点：断电崩溃处理? --> 事件：解决问题的核心
          - 业务逻辑处理器的状态是由输入事件驱动的
          - 输入事件被持久化保存起来(NOSQL存储的基于事件的事务实现)
            - LAMX提供业务逻辑处理的快照，从快照还原（每天晚上系统不繁忙时构建快照）
            - LAMX保持多个业务逻辑处理器同时运行，每个输入事件由多个处理器处理，只有一个处理器输出有效
            - 通过事件驱动可以在处理器之间微秒速度切换，每晚创建快照，每晚重启业务逻辑处理器
        
4. 性能优化
    - 性能关键：按顺序地做事 （并行做就聪明？）

5.  Disruptor的输入和输出
    - 原始输入（消息形式）转换为业务逻辑器能够处理的形式
    - 事件源依赖于所有输入事件的持久化
    - 整个架构依赖于业务逻辑器的集群
    - 输出事件需要进行转换以便在网络上传输
    ![Disruptor的输入和输出](http://ifeve.com/wp-content/uploads/2013/01/input-activity.png)
     - 如果复制和日志是比较慢的，所有这些业务都应该相对独立，需要在业务逻辑处理器处理之前完成
     - 不同于业务逻辑处理器需要根据交易自然先后进行交易，这些都是需要的并发机制

6.  并发机制：Disruptor组件
    - 一个事件监听或消息机制
       - 在队列中一边生产者放入消息，另外一边消费者**并行**取出处理
    - 队列内部数据结构：RingBuffer
       - 每个生产者消费者写入自己次序计数器，能够读取对方的计数器
       - 生产者能够读取消费者的计数器确保其在没有锁的情况下是可写的
       - 消费者也要通过计算器在另一个消费者完成后确保它一次只处理一次消息
     ![Disruptor](http://ifeve.com/wp-content/uploads/2013/01/disruptor.png)  
     
 ![生产者与消费者协同方式](https://images2015.cnblogs.com/blog/1047231/201702/1047231-20170208183601338-912624234.png)    
- 在这个结构下，每个消费者拥有各自独立的事件序号Sequence，消费者之间不存在共享竞态
- SequenceBarrier1监听RingBuffer的序号cursor，消费者B与C通过SequenceBarrier1等待可消费事件
- SequenceBarrier2除了监听cursor，同时也监听B与C的序号Sequence，从而将最小的序号返回给消费者D，由此实现了D依赖B与C的逻辑

![轮询消息并处理](https://pic1.zhimg.com/80/v2-24d185bac2b879459aa588cfa16651f0_hd.jpg)


###　RingBuffer多生产者写入
- 参考资料：https://www.alicharles.com/article/disruptor/disruptor-ringbuffer-muti-write/
1.  多生产者MultiProducerSequencer申请下一个节点 
   ![MultiProducerSequencer](https://www.alicharles.com/images/2016/09/20160905225141_94026.png)  
   - 可能出现某些Entry正在被生产者写入但还没有提交的情况  
    ![MultiProducerSequencer](https://www.alicharles.com/images/2016/09/20160905225159_84973.png)   
        
          
        
                    