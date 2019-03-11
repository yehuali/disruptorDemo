package com.example.disruptor;

public interface EventHandler<T> {
    /**
     * 当发布者将事件发布到{@link RingBuffer}时调用
     * {@link BatchEventProcessor}会批量读取来自{@link RingBuffer}的消息,
     *     其中一个批处理是所有可用的事件无需等待任何新事件的到来,这对于需要的事件处理程序非常有用执行较慢的操作，例如I/O
     *     因为它们可以将多个事件的数据组合成一个事件操作。实现应确保始终在endOfBatch为true时执行该操作,那条消息和下一条消息之间的时间是确定的
     * @param event
     * @param sequence
     * @param endOfBatch
     * @throws Exception
     */
    void onEvent(T event, long sequence, boolean endOfBatch) throws Exception;
}
