package com.example.disruptor;

/**
 * 此接口的实现者必须提供一个long类型的值表示当前游标值
 * 从{@link SequenceGroups#addSequences(Object, java.util.concurrent.atomic.AtomicReferenceFieldUpdater, Cursored, Sequence...)}动态添加/移除期间使用
 */
public interface Cursored {
    long getCursor();
}
