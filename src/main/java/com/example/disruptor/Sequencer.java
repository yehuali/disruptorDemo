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

    //确认序列是否已发布且事件可用;非阻塞
    boolean isAvailable(long sequence);

    /**
     * 获取可以从环缓冲区安全地读取的最高序列号
     * 根据在Sequencer实现中，此调用可能需要扫描多个值在Sequencer中，扫描范围从nextSequence到availableSequence
     * 如果没有可用的值nextSequence，返回值将是nextSequence - 1
     * 要正确工作，consumer 应该传递一个值比最后一个成功处理的序列高1
     * @param nextSequence
     * @param availableSequence
     * @return
     */
    long getHighestPublishedSequence(long nextSequence, long availableSequence);

}
