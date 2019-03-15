package com.example.disruptor;

import com.example.disruptor.util.Util;
import sun.misc.Unsafe;

import java.util.concurrent.locks.LockSupport;

public class MultiProducerSequencer extends AbstractSequencer {

    private static final Unsafe UNSAFE = Util.getUnsafe();
    private static final long BASE = UNSAFE.arrayBaseOffset(int[].class);
    private static final long SCALE = UNSAFE.arrayIndexScale(int[].class);

    private final Sequence gatingSequenceCache = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);

    //availableBuffer跟踪每个ringbuffer槽的状态
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

    @Override
    public long next() {
        return next(1);
    }

    @Override
    public long next(int n) {
        if (n < 1)
        {
            throw new IllegalArgumentException("n must be > 0");
        }

        long current;
        long next;

        do{
            current = cursor.get();
            next = current + n;

            long wrapPoint = next - bufferSize;
            long cachedGatingSequence = gatingSequenceCache.get();
            /**
             * 生产者覆盖消费者 或者 消费者超过生产者
             */
            if (wrapPoint > cachedGatingSequence || cachedGatingSequence > current){
                long gatingSequence = Util.getMinimumSequence(gatingSequences, current);

                if (wrapPoint > gatingSequence)
                {
                    LockSupport.parkNanos(1); // TODO, should we spin based on the wait strategy?
                    continue;
                }

                gatingSequenceCache.set(gatingSequence);
            } else if (cursor.compareAndSet(current, next)){
                break;
            }

        }while(true);
        return next;
    }

    @Override
    public void publish(long sequence) {
        setAvailable(sequence);
        waitStrategy.signalAllWhenBlocking();
    }

    /**
     * 下面的方法用于availableBuffer标志
     *  主要原因是避免在发布程序线程之间共享sequence对象
     *  保持单指针跟踪开始和结束需要协调在线程之间
     * take the sequence value and mask off the lower portion of the sequence as the index into the buffer (indexMask)（又称模运算符）
     * 首先，我们有一个约束，即游标与最小值(gating sequence)之间的增量永远不会大于缓冲区大小(其中的代码next/tryNext在序列中负责这个)
     * 序列的上半部分成为检查可用性的值
     * 它告诉我们已经绕着环形缓冲区转了多少圈(也就是除法)
     * 因为我们不能在没有gating sequences移除之前进行包装（最小gating sequence是最近在buffer可用的）
     * 当我们有了新数据并成功地声明了一个插槽时，我们可以简单地写在上面。
     *
     * @param sequence
     */
    private void setAvailable(final long sequence)
    {
        setAvailableBufferValue(calculateIndex(sequence), calculateAvailabilityFlag(sequence));
    }

    private int calculateIndex(final long sequence)
    {
        return ((int) sequence) & indexMask;
    }


    private int calculateAvailabilityFlag(final long sequence)
    {
        /**
         *  sequence >>> indexShift，其实就是绕环形数组的圈数
         *  而消费者在获取最大的有效序列,也是通过与availableBuffer对应的序列值进行比较进行判断
         */
        return (int) (sequence >>> indexShift);
    }

    @Override
    public SequenceBarrier newBarrier(Sequence... sequencesToTrack) {
        return new ProcessingSequenceBarrier(this, waitStrategy, cursor, sequencesToTrack);
    }

    @Override
    public boolean isAvailable(long sequence) {
        int index = calculateIndex(sequence);
        int flag = calculateAvailabilityFlag(sequence);
        long bufferAddress = (index * SCALE) + BASE;
        return UNSAFE.getIntVolatile(availableBuffer, bufferAddress) == flag;
    }

    @Override
    public long getHighestPublishedSequence(long lowerBound, long availableSequence) {
        for (long sequence = lowerBound; sequence <= availableSequence; sequence++){ //从lowerBound开始遍历，在RingBuffer中找到最大的已经被publish事件的slot
            if (!isAvailable(sequence)) {
                return sequence - 1;
            }
        }
        return availableSequence;
    }

}
