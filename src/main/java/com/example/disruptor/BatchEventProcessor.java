package com.example.disruptor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 为什么叫BatchEventProcessor
 * --->可以看run()方法每次waitFor获取的availableSequence是当前能够使用的最大值，然后再循环处理这些数据
 *     --->当消费者有瞬时抖动时，导致暂时落后生产者，可以在下一次循环中，批量处理落后的事件
 * @param <T>
 */
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

            notifyStart();
            try{
                if (running.get() == RUNNING){
                    processEvents();
                }
            }finally {
                notifyShutdown();
                running.set(IDLE);
            }
        }

    }

    private void processEvents(){
        T event = null;
        long nextSequence = sequence.get() + 1L;
        while(true){
            try{
                // availableSequence返回的是可用的最大值
                final long availableSequence = sequenceBarrier.waitFor(nextSequence);//使用给定的等待策略去等待下一个序列可用

                //批处理在此处得以体现
                while (nextSequence <= availableSequence){
                    event = dataProvider.get(nextSequence);
                    eventHandler.onEvent(event, nextSequence, nextSequence == availableSequence);
                    nextSequence++;
                }
                //eventHandler处理完毕后，更新当前序号
                sequence.set(availableSequence);

            }catch (Exception e){

            }

        }
    }

    private void notifyShutdown()
    {
//        if (eventHandler instanceof LifecycleAware)
//        {
//            try
//            {
//                ((LifecycleAware) eventHandler).onShutdown();
//            }
//            catch (final Throwable ex)
//            {
//                exceptionHandler.handleOnShutdownException(ex);
//            }
//        }
    }

    private void notifyStart()
    {
//        if (eventHandler instanceof LifecycleAware)
//        {
//            try
//            {
//                ((LifecycleAware) eventHandler).onStart();
//            }
//            catch (final Throwable ex)
//            {
//                exceptionHandler.handleOnStartException(ex);
//            }
//        }
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
