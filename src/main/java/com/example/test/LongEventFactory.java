package com.example.test;

import com.example.disruptor.EventFactory;

/**
 * 事件预分配
 */
public class LongEventFactory implements EventFactory<LongEvent> {
    @Override
    public LongEvent newInstance() {
        return new LongEvent();
    }
}
