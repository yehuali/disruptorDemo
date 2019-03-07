package com.example.test;

import com.example.disruptor.dsl.Disruptor;
import com.example.disruptor.util.DaemonThreadFactory;

public class LongEventMain {
    public static void main(String[] args) {
        int bufferSize = 1024;
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE);
    }
}
