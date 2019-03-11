package com.example.disruptor.util;

import com.example.disruptor.Sequence;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

public final class Util {
    private static final Unsafe THE_UNSAFE;

    static
    {
        try
        {
            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>()
            {
                public Unsafe run() throws Exception
                {
                    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return (Unsafe) theUnsafe.get(null);
                }
            };

            THE_UNSAFE = AccessController.doPrivileged(action);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to load unsafe", e);
        }
    }

    public static Unsafe getUnsafe()
    {
        return THE_UNSAFE;
    }

    /**
     * 计算所提供整数以log以2为底的对数，本质上报告位置最高位的
     * @param i 用于计算log2的值
     * @return
     */
    public static int log2(int i)
    {
        int r = 0;
        while ((i >>= 1) != 0)
        {
            ++r;
        }
        return r;
    }

    /**
     *从{@link Sequence}数组中获取最小序列
     *
     * @param sequences
     * @param minimum
     * @return
     */
    public static long getMinimumSequence(final Sequence[] sequences, long minimum){
        for (int i = 0, n = sequences.length; i < n; i++){
            long value = sequences[i].get();
            minimum = Math.min(minimum, value);
        }
        return minimum;
    }

}
