package com.example.disruptor;

import java.util.concurrent.atomic.AtomicInteger;

public final class BatchEventProcessor<T> implements EventProcessor {
    private static final int IDLE = 0;
    private static final int HALTED = IDLE + 1;
    private static final int RUNNING = HALTED + 1;

    private final AtomicInteger running = new AtomicInteger(IDLE);
    private final SequenceBarrier sequenceBarrier;
    private final DataProvider<T> dataProvider;
    private final EventHandler<? super T> eventHandler;
    private final Sequence sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);


    private ExceptionHandler<? super T> exceptionHandler = new FatalExceptionHandler();

    /**
     * 构造一个{@link EventProcessor}，当{@link EventHandler#onEvent(Object, long, boolean)}方法返回的时候，它将通过更新序列来自动跟踪进程
     * @param dataProvider
     * @param sequenceBarrier
     * @param eventHandler
     */
    public BatchEventProcessor(
            final DataProvider<T> dataProvider,
            final SequenceBarrier sequenceBarrier,
            final EventHandler<? super T> eventHandler)
    {
        this.dataProvider = dataProvider;
        this.sequenceBarrier = sequenceBarrier;
        this.eventHandler = eventHandler;
    }

    /**
     * 让另一个线程在halt()之后重新运行这个方法是可以的
     */
    @Override
    public void run() {
        if (running.compareAndSet(IDLE, RUNNING)){
            sequenceBarrier.clearAlert();
        }

    }


    public void setExceptionHandler(final ExceptionHandler<? super T> exceptionHandler)
    {
        if (null == exceptionHandler)
        {
            throw new NullPointerException();
        }

        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public Sequence getSequence() {
        return sequence;
    }
}
