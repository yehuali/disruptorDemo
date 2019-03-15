package com.example.disruptor;

/**
 * 协调barrier，跟踪发布者的cursor(游标)和序列依赖{@link EventProcessor} 处理数据结构
 */
public interface SequenceBarrier {
    void clearAlert();

    //等待给定的序列可供使用
    long waitFor(long sequence) throws Exception;

    void checkAlert() throws Exception;
}
