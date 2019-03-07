package com.example.disruptor;

import com.example.disruptor.util.Util;
import sun.misc.Unsafe;

public class MultiProducerSequencer extends AbstractSequencer {

    private static final Unsafe UNSAFE = Util.getUnsafe();
    private static final long BASE = UNSAFE.arrayBaseOffset(int[].class);
    private static final long SCALE = UNSAFE.arrayIndexScale(int[].class);

    private final int[] availableBuffer;
    private final int indexMask;
    private final int indexShift;

    public MultiProducerSequencer(int bufferSize, final WaitStrategy waitStrategy)
    {
        super(bufferSize, waitStrategy);
        availableBuffer = new int[bufferSize];
        indexMask = bufferSize - 1;
        indexShift = Util.log2(bufferSize);
        initialiseAvailableBuffer();
    }

    private void initialiseAvailableBuffer()
    {
        for (int i = availableBuffer.length - 1; i != 0; i--)
        {
            setAvailableBufferValue(i, -1);
        }

        setAvailableBufferValue(0, -1);
    }


    private void setAvailableBufferValue(int index, int flag)
    {
        long bufferAddress = (index * SCALE) + BASE;
        UNSAFE.putOrderedInt(availableBuffer, bufferAddress, flag);
    }
}
