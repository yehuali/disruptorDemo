### 参考资源： https://www.jianshu.com/p/252e27863822

### Java类的加载过程
- 问题：
    - Java类文件是如何加载到虚拟机的
    - 类对象和方法是以什么数据结构存在于虚拟机中
    - 虚方法、实例方法和静态方法是如何调用的
    
- 内存划分
    - 内存划分成Java堆、方法区、Java栈、本地方法栈和PC寄存器
        - Java栈和本地方法栈用于方法之间的调用，进栈出栈的过程
        - Java堆用于存放对象
        - 方法区分成PermGen(永久代)和CodeCache
            - PermGen存放Java类的相关信息，如静态变量、成员方法和抽象方法等
            - CodeCache存放JIT编译之后的本地代码
            
- 对象模型
    - 设计了一个oop/klass model 
        - oop（Ordinary Object Pointer ：普通对象指针）：表示对象的实例信息
        - klass :保存描述元数据 
    - 为何设计oop/klass二分模型的实现
        - 不想让每个对象都包含vtbl（虚方法表）
        - oop不含有任何虚函数，虚函数保存于klass中，可以进行method dispatch
            - oopDesc对象包含两部分数据：_mark和 _metadata
                - _mark是markOop类型对象，用于存储对象自身的运行时数据
                — _metadata是一个结构体
                - _klass建立了oop对象与klass对象之间的联系

- HotSpot如何加载并解析class文件
    - class文件在虚拟机的生命周期：加载、验证、准备、解析、初始化和卸载
        - loadClass方法实现了双亲委派的类加载机制
        - loadClass最终执行native方法defineClass1进行类的加载：读取class文件的二进制数据到虚拟机进行解析

- class文件的解析
    - 在HotSpot中，其实现位于ClassLoader.c文件中
        - 验证全限定类名的长度
        - 处理stream数据流，并生成Klass对象
        - 对class文件的数据流进行解析
        - 创建一个与之对应的instanceKlass对象
            - 初始化一个空instanceKlass对象，并由后续逻辑进行数据的填充
            - 该方法的返回类型却是klassOop类型
            - 

### Java对象的内存分配            
- 几种分配方式
    - 从线程的局部缓冲区分配临时内存
    - 从内存堆中分配临时内存
    - 从内存堆中分配永久内存 
    
- 新建一个对象时
    - 由对应的instanceKlass对象计算出需要多大的内存  
    - 


                       
