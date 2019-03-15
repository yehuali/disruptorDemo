package com.example.disruptor;


public interface WaitStrategy {

    long waitFor(long sequence, Sequence cursor, Sequence dependentSequence, SequenceBarrier barrier)
            throws Exception;

    //实现应该向等待游标的{@link EventProcessor}发出信号
    void signalAllWhenBlocking();
}
