package com.example.disruptor.dsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * 提供一个存储库机制来将{@link EventHandler}s与{@link EventProcessor}s关联起来
 * @param <T>
 */
class ConsumerRepository<T> implements Iterable<ConsumerInfo> {

    private final Collection<ConsumerInfo> consumerInfos = new ArrayList<>();

    @Override
    public Iterator<ConsumerInfo> iterator() {
        return consumerInfos.iterator();
    }
}
