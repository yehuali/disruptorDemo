package com.example.disruptor;

/**
 * 在跟踪依赖{{@link Sequence}},协调声明访问数据结构的序列
 */
public interface Sequencer extends Cursored, Sequenced {
    //设置-1为序列起始点
    long INITIAL_CURSOR_VALUE = -1L;

    /**
     * 创建一个新的SequenceBarrier，事件处理器将使用它来跟踪哪些消息可从给定要跟踪的序列列表的环形缓冲区中读取
     * @param sequencesToTrack  新构建的barrier将等待的所有序列
     * @return 一个将跟踪指定序列的序列屏障
     */
    SequenceBarrier newBarrier(Sequence... sequencesToTrack);

    /**
     * 指定gating sequences到Disruptor实例中，他们将安全原子地添加到gating sequences列表中
     * @param gatingSequences
     */
    void addGatingSequences(Sequence... gatingSequences);

    boolean removeGatingSequence(Sequence sequence);

}
