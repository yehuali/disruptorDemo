package com.example.disruptor;

/**
 * 调用{@link RingBuffer} 预填充所有事件以填充RingBuffer
 * @param <T> 事件实现存储数据，以便在事件的交换或并行协调期间的共享
 */
public interface EventFactory<T> {
    /**
     * 实现应该实例化一个事件对象，并在可能的情况下分配所有内存
     * @return
     */
    T newInstance();
}
