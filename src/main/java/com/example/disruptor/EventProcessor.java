package com.example.disruptor;

/**
 * 事件处理器需要是一个runnable的实现，它将轮询来自{@link RingBuffer}的事件
 *  使用适当的等待策略。您不太可能需要自己实现这个接口。
 *  在第一个中使用{@link EventHandler}接口和预提供的BatchEventProcessor实例
 *
 *  事件处理器通常与执行线程相关联
 */
public interface EventProcessor extends Runnable {
    Sequence getSequence();
}
