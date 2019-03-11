package com.example.disruptor.dsl;

import java.util.concurrent.Executor;

public interface ConsumerInfo {
    void start(Executor executor);
    void markAsUsedInBarrier();
}
