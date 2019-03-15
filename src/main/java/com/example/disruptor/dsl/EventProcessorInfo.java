package com.example.disruptor.dsl;

import com.example.disruptor.EventHandler;
import com.example.disruptor.EventProcessor;
import com.example.disruptor.SequenceBarrier;

import java.util.concurrent.Executor;

/**
 * 包装器类，将特定的事件处理阶段绑定在一起
 * 跟踪事件处理器实例、事件处理程序实例和连接到该阶段的序列屏障
 * @param <T>
 */
public class EventProcessorInfo<T> implements ConsumerInfo {

    private final EventProcessor eventprocessor;
    private final EventHandler<? super T> handler;
    private final SequenceBarrier barrier;
    private boolean endOfChain = true;

    EventProcessorInfo(
            final EventProcessor eventprocessor, final EventHandler<? super T> handler, final SequenceBarrier barrier)
    {
        this.eventprocessor = eventprocessor;
        this.handler = handler;
        this.barrier = barrier;
    }



    @Override
    public void start(Executor executor) {
        executor.execute(eventprocessor);
    }

    @Override
    public void markAsUsedInBarrier() {
        endOfChain = false;
    }
}
