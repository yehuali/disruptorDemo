package com.example.disruptor;

public final class ProcessingSequenceBarrier implements SequenceBarrier{

    private final WaitStrategy waitStrategy;
    private final Sequence dependentSequence;
    private volatile boolean alerted = false;
    private final Sequence cursorSequence;
    private final Sequencer sequencer;

    ProcessingSequenceBarrier(
            final Sequencer sequencer,
            final WaitStrategy waitStrategy,
            final Sequence cursorSequence,
            final Sequence[] dependentSequences)
    {
        this.sequencer = sequencer;
        this.waitStrategy = waitStrategy;
        this.cursorSequence = cursorSequence;
        if (0 == dependentSequences.length)
        {
            dependentSequence = cursorSequence;
        }
        else
        {
            dependentSequence = new FixedSequenceGroup(dependentSequences);
        }
    }

    @Override
    public void clearAlert() {
        alerted = false;
    }

    @Override
    public long waitFor(long sequence) throws Exception {
        checkAlert();
        long availableSequence = waitStrategy.waitFor(sequence, cursorSequence, dependentSequence, this);
        if (availableSequence < sequence) {
            return availableSequence;
        }
        /**
         * 获取消费者可以消费的最大的可用序号，支持批处理效应，提升处理效率
         *
         */
        return sequencer.getHighestPublishedSequence(sequence, availableSequence);
    }

    @Override
    public void checkAlert() throws Exception {
        if (alerted)
        {
            throw new Exception("checkAlert发生异常");
        }
    }
}
