package com.example.disruptor;

import com.example.disruptor.util.ThreadHints;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingWaitStrategy implements WaitStrategy {
    private final Lock lock = new ReentrantLock();
    private final Condition processorNotifyCondition = lock.newCondition();

    @Override
    public long waitFor(long sequence, Sequence cursorSequence, Sequence dependentSequence, SequenceBarrier barrier) throws Exception {
        long availableSequence;
        if (cursorSequence.get() < sequence){
            lock.lock();
            try{
                while (cursorSequence.get() < sequence){
                    // 检查alert状态。如果不检查将导致不能关闭Disruptor。
                    barrier.checkAlert();
                    processorNotifyCondition.await();
                }
            }finally {
                lock.unlock();
            }
        }
        /**
         * 给定序号大于上一个消费者组最慢消费者（如果当前消费者为第一组则和生产者游标序号比较）序号时，需要等待
         * 不能超前消费上一个消费者组未消费完毕的事件
         * 此时已能保证生产者有新事件，如果进入循环，说明上一组消费者还未消费完毕，而通常消费者都是较快完成任务
         * ，所以考虑使用自旋的方式等待上一组消费者完成消费
         */
        while ((availableSequence = dependentSequence.get()) < sequence){
            barrier.checkAlert();
            ThreadHints.onSpinWait();
        }

        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking() {
        lock.lock();
        try
        {
            processorNotifyCondition.signalAll();
        }
        finally
        {
            lock.unlock();
        }
    }
}
