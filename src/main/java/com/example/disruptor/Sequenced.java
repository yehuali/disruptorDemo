package com.example.disruptor;

public interface Sequenced {

    int getBufferSize();

    //要求按顺序发布下一个事件
    long next();

    /**
     * 声明接下来要发布的n个事件。这是用于批量事件生成的
     * 使用批量生产需要一点细心和一些数学知识
     * @param n
     * @return
     */
    long next(int n);

    /**
     * 发布一个序列，事件已经填充时调用
     * @param sequence
     */
    void publish(long sequence);
}
