package com.example.disruptor.dsl;

import com.example.disruptor.EventFactory;
import com.example.disruptor.RingBuffer;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * 一个DSL风格的API，用于在ringBuffer设置disruptor模式（也就是构建器模式）
 * 用两个事件处理程序设置disruptor的简单示例必须按照以下顺序处理事件
 * <pre>
 * <code>Disruptor&lt;MyEvent&gt; disruptor = new Disruptor&lt;MyEvent&gt;(MyEvent.FACTORY, 32, Executors.newCachedThreadPool());
 * EventHandler&lt;MyEvent&gt; handler1 = new EventHandler&lt;MyEvent&gt;() { ... };
 * EventHandler&lt;MyEvent&gt; handler2 = new EventHandler&lt;MyEvent&gt;() { ... };
 * disruptor.handleEventsWith(handler1);
 * disruptor.after(handler1).handleEventsWith(handler2);
 *
 * RingBuffer ringBuffer = disruptor.start();</code>
 * </pre>
 * @param <T> 使用的事件类型
 */
public class Disruptor<T> {

    private final RingBuffer<T> ringBuffer;
    private final Executor executor;

    /**
     * 新建Disruptor,默认为{BlockingWaitStrategy}和 {@link ProducerType}.MULTI
     * @param eventFactory 在ring Buffer中创建事件的工厂
     * @param ringBufferSize
     * @param threadFactory 一个为处理器创建线程 {@link ThreadFactory}
     */
    public Disruptor(final EventFactory<T> eventFactory, final int ringBufferSize, final ThreadFactory threadFactory)
    {
        this(RingBuffer.createMultiProducer(eventFactory, ringBufferSize), new BasicExecutor(threadFactory));
    }

    private Disruptor(final RingBuffer<T> ringBuffer, final Executor executor)
    {
        this.ringBuffer = ringBuffer;
        this.executor = executor;
    }
}
