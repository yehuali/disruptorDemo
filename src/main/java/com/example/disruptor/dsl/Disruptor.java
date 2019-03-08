package com.example.disruptor.dsl;

import com.example.disruptor.EventFactory;
import com.example.disruptor.RingBuffer;
import com.example.disruptor.Sequence;
import com.example.disruptor.SequenceBarrier;
import javafx.event.EventHandler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final ConsumerRepository<T> consumerRepository = new ConsumerRepository<>();
    private final AtomicBoolean started = new AtomicBoolean(false);

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

    private void checkNotStarted()
    {
        if (started.get())
        {
            throw new IllegalStateException("All event handlers must be added before calling starts.");
        }
    }

    /**
     * 设置事件处理程序来处理来自环形缓冲区的事件,这些处理程序将处理事件
     * 这个方法可以作为一个链的开始,例如，如果处理程序<code>A</code>必须处理事件之前的处理程序<code>B</code>
     * < pre > <code> dw.handleEventsWith (A) (B), < /code> < / pre >
     * 这个调用是附加的，但是通常在设置Disruptor实例</p>时应该只调用一次
     * @param handlers
     * @return {@link EventHandlerGroup}可以用来链接依赖项
     */
    @SafeVarargs
    public final EventHandlerGroup<T> handleEventsWith(final EventHandler<? super T>... handlers)
    {
        return createEventProcessors(new Sequence[0], handlers);
    }

    EventHandlerGroup<T> createEventProcessors(
            final Sequence[] barrierSequences,
            final EventHandler<? super T>[] eventHandlers)
    {
        checkNotStarted();

        final Sequence[] processorSequences = new Sequence[eventHandlers.length];
        final SequenceBarrier barrier = ringBuffer.newBarrier(barrierSequences);

        for (int i = 0, eventHandlersLength = eventHandlers.length; i < eventHandlersLength; i++)
        {
            final EventHandler<? super T> eventHandler = eventHandlers[i];

            final BatchEventProcessor<T> batchEventProcessor =
                    new BatchEventProcessor<>(ringBuffer, barrier, eventHandler);

            if (exceptionHandler != null)
            {
                batchEventProcessor.setExceptionHandler(exceptionHandler);
            }

            consumerRepository.add(batchEventProcessor, eventHandler, barrier);
            processorSequences[i] = batchEventProcessor.getSequence();
        }

        updateGatingSequencesForNextInChain(barrierSequences, processorSequences);

        return new EventHandlerGroup<>(this, consumerRepository, processorSequences);
    }
}
