package com.example.disruptor.dsl;

import com.example.disruptor.EventHandler;
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

    @SafeVarargs
    public final EventHandlerGroup<T> then(final EventHandler<? super T>... handlers)
    {
        return handleEventsWith(handlers);
    }

    @SafeVarargs
    public final EventHandlerGroup<T> handleEventsWith(final EventHandler<? super T>... handlers)
    {
        return disruptor.createEventProcessors(sequences, handlers);
    }
}
