package com.example.disruptor.dsl;

import com.example.disruptor.Sequence;

import java.util.Arrays;

public class EventHandlerGroup<T> {
    private final Disruptor<T> disruptor;
    private final ConsumerRepository<T> consumerRepository;
    private final Sequence[] sequences;

    EventHandlerGroup(
            final Disruptor<T> disruptor,
            final ConsumerRepository<T> consumerRepository,
            final Sequence[] sequences)
    {
        this.disruptor = disruptor;
        this.consumerRepository = consumerRepository;
        this.sequences = Arrays.copyOf(sequences, sequences.length);
    }
}
