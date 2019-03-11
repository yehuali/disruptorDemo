package com.example.disruptor.dsl;

import com.example.disruptor.EventHandler;
import com.example.disruptor.EventProcessor;
import com.example.disruptor.Sequence;
import com.example.disruptor.SequenceBarrier;

import java.util.*;

/**
 * 提供一个存储库机制来将{@link EventHandler}s与{@link EventProcessor}s关联起来
 * @param <T>
 */
class ConsumerRepository<T> implements Iterable<ConsumerInfo> {

    private final Collection<ConsumerInfo> consumerInfos = new ArrayList<>();

    private final Map<EventHandler<?>, EventProcessorInfo<T>> eventProcessorInfoByEventHandler =
            new IdentityHashMap<>();

    private final Map<Sequence, ConsumerInfo> eventProcessorInfoBySequence =
            new IdentityHashMap<>();

    public void add(
            final EventProcessor eventprocessor,
            final EventHandler<? super T> handler,
            final SequenceBarrier barrier)
    {
        final EventProcessorInfo<T> consumerInfo = new EventProcessorInfo<>(eventprocessor, handler, barrier);
        eventProcessorInfoByEventHandler.put(handler, consumerInfo);
        eventProcessorInfoBySequence.put(eventprocessor.getSequence(), consumerInfo);
        consumerInfos.add(consumerInfo);
    }

    public void unMarkEventProcessorsAsEndOfChain(final Sequence... barrierEventProcessors)
    {
        for (Sequence barrierEventProcessor : barrierEventProcessors)
        {
            getEventProcessorInfo(barrierEventProcessor).markAsUsedInBarrier();
        }
    }

    private ConsumerInfo getEventProcessorInfo(final Sequence barrierEventProcessor)
    {
        return eventProcessorInfoBySequence.get(barrierEventProcessor);
    }

    @Override
    public Iterator<ConsumerInfo> iterator() {
        return consumerInfos.iterator();
    }
}
