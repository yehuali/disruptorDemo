package com.example.disruptor;

public interface DataProvider<T> {
    T get(long sequence);
}
