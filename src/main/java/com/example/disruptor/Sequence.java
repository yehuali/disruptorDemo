package com.example.disruptor;

import com.example.disruptor.util.Util;
import sun.misc.Unsafe;

class LhsPadding
{
    protected long p1, p2, p3, p4, p5, p6, p7;
}

class Value extends LhsPadding
{
    protected volatile long value;
}

class RhsPadding extends Value
{
    protected long p9, p10, p11, p12, p13, p14, p15;
}

public class Sequence extends RhsPadding {

    static final long INITIAL_VALUE = -1L;
    private static final Unsafe UNSAFE;
    private static final long VALUE_OFFSET;

    /**
     *  获取Unsafe，通过Unsafe获取Sequence中的value的地址，根据这个地址CAS更新
     */
    static
    {
        UNSAFE = Util.getUnsafe();
        try
        {
            VALUE_OFFSET = UNSAFE.objectFieldOffset(Value.class.getDeclaredField("value"));
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Sequence()
    {
        this(INITIAL_VALUE);
    }


    // 创建具有指定初始值的序列
    public Sequence(final long initialValue)
    {
        UNSAFE.putOrderedLong(this, VALUE_OFFSET, initialValue);
    }

    public long get()
    {
        return value;
    }

    /**
     * Perform an ordered write of this sequence.  The intent is
     * a Store/Store barrier between this write and any previous
     * store.
     *
     * @param value The new value for the sequence.
     */
    public void set(final long value)
    {
        UNSAFE.putOrderedLong(this, VALUE_OFFSET, value);
    }


    /**
     * Performs a volatile write of this sequence.  The intent is
     * a Store/Store barrier between this write and any previous
     * write and a Store/Load barrier between this write and any
     * subsequent volatile read.
     *
     * @param value The new value for the sequence.
     */
    public void setVolatile(final long value)
    {
        UNSAFE.putLongVolatile(this, VALUE_OFFSET, value);
    }


}
