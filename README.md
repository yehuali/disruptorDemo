 - 参考资料：https://blog.csdn.net/zhxdick/article/details/51549399
1. 解决CPU 伪共享
2. 无锁队列实现
    - 传统队列：两个指针：头指针 和 尾指针
    - Disruptor:
       - Producer而言只有头指针，而且锁是乐观锁
       - 标准Disruptor应用中，只有一个生产者，避免头指针锁的争用
                           
3. 工作流程
    - 初始化时，RingBuffer规定了总大小（可以容纳的槽：必须是2的n次方 ---> 取模转变为取与运算）
      - RingBuffer定位：对Ringbuffer的大小取余定位
    - Producer向Ringbuffer填充元素
       - 从Ringbuffer读取下一个Sequence
       - 在Sequence位置的槽填充数据
       - 发布
    - Consumer消费RingBuffer中的数据
      - 通过SequenceBarrier来协调不同的Consumer的消费先后顺序
      - 获取下一个消费位置Sequence
     
    - Producer在RingBuffer写满时，会从头继续写，替换以前数据
      - 如果SequenceBarrier指向下一个位置，则会阻塞这个位置被消费完成
      - Consumer同理
     
4. CPU缓存结构
   - CPU存在L1缓存，L2缓存，L3缓存来缓存内存中的数据
       - CPU缓存：每次从内存中取出一行内存（缓存行）
          - 查看机器缓存行长度
   - L3是单个插槽上的所有CPU核共享的，L1,L2是各自CPU核独占的      
          
5. Java对象占用内存
    - 一般电脑CPU缓存行是64字节
        - 一个缓存行可以存8个long类型的变量
            - 考虑极端情况（Cache line开头或者结尾是value）
            
6.  Disruptor工作流程总结
    - 消费者注册
    ![消费者注册](https://github.com/yehuali/disruptorDemo/blob/master/image/消费者注册.png)
    - 初始化Disruptor流程（理清类之间的关系） 
    ![初始化Disruptor流程](https://github.com/yehuali/disruptorDemo/blob/master/image/初始化Disruptor流程.jpg) 
    - Disruptor启动流程（启动消费者线程）
    ![Disruptor启动流程](https://github.com/yehuali/disruptorDemo/blob/master/image/Disruptor运行流程.jpg)
    - Disruptor事件发布
    ![Disruptor事件发布](https://github.com/yehuali/disruptorDemo/blob/master/image/Disruptor事件发布.jpg)          
    - 消费者消费流程
    ![消费者消费流程](https://github.com/yehuali/disruptorDemo/blob/master/image/消费者消费流程.jpg)
            
   
                              
  