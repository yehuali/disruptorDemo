package com.example.disruptor;

public abstract class AbstractSequencer implements Sequencer {

    protected final Sequence cursor = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
    protected final int bufferSize;
    protected final WaitStrategy waitStrategy;

    /**
     * 使用指定的缓冲区大小和等待策略创建。
     * @param bufferSize  entries的数量，必须是2的正幂
     * @param waitStrategy  sequencer使用的等待策略
     */
    public AbstractSequencer(int bufferSize, WaitStrategy waitStrategy)
    {
        if (bufferSize < 1)
        {
            throw new IllegalArgumentException("bufferSize must not be less than 1");
        }
        if (Integer.bitCount(bufferSize) != 1)
        {
            throw new IllegalArgumentException("bufferSize must be a power of 2");
        }

        this.bufferSize = bufferSize;
        this.waitStrategy = waitStrategy;
    }

    public long getCursor() {
        return cursor.get();
    }

    public final int getBufferSize() {
        return bufferSize;
    }
}
