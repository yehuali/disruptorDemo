package com.example.disruptor;

public interface EventTranslatorOneArg<T,A> {
    void translateTo(T event, long sequence, A arg0);
}
