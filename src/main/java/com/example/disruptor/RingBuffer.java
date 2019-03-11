package com.example.disruptor;

import com.example.disruptor.dsl.ProducerType;
import com.example.disruptor.util.Util;
import sun.misc.Unsafe;


/**
 * 参考：
 *  http://www.ibigdata.io/?p=142
 *  https://my.oschina.net/u/3101713/blog/820740
 */

/**
 * RingBufferPad作用：通过7个long变量去填充缓存行
 */
abstract class RingBufferPad
{
    protected long p1, p2, p3, p4, p5, p6, p7;
}

/**
 * 维护了一个Event对象的环形数组
 * @param <E>
 */
abstract class RingBufferFields<E> extends RingBufferPad
{
    //数组中一共需要填充的个数
    private static final int BUFFER_PAD;
    //数组的开始地址，是整个数组开始地址 + BUFFER_PAD个引用的偏移量
    private static final long REF_ARRAY_BASE;
    //一个引用占用的字节数的幂次方
    private static final int REF_ELEMENT_SHIFT;
    private static final Unsafe UNSAFE = Util.getUnsafe();

    static
    {
        /**
         * UNSAFE.arrayIndexScale 是获取一个数组在内存中的scale，也就是每个数组元素在内存中的大小
         * object数组引用长度，32位为4字节，64位为8字节
         */
        final int scale = UNSAFE.arrayIndexScale(Object[].class);

        /**
         * Oop指针是4还是未压缩的8也可以通过unsafe.arrayIndexScale(Object[].class)来获得
         */
        if (4 == scale)
        {
            REF_ELEMENT_SHIFT = 2;
        }
        else if (8 == scale)
        {
            REF_ELEMENT_SHIFT = 3;
        }
        else
        {
            throw new IllegalStateException("Unknown pointer size");
        }
        //需要填充128字节，缓存行长度一般是128字节
        BUFFER_PAD = 128 / scale;
        // Including the buffer pad in the array base offset
        /**
         *  数组元素定位：
         *  1.arrayBaseOffset：本地方法，可以获取数组第一个元素的偏移地地址
         *  2.arrayIndexScale：本地方法，获取数组的转换因子，也就是数组中元素的增量地址
         *  索引为 i 的元素可以使用如下代码定位：
         *     int baseOffset = unsafe.arrayBaseOffset(array.getClass());
         *     int indexScale = unsafe.arrayIndexScale(array.getClass());
         *     baseOffset + i*indexScale
         */

        /**
         *  BUFFER_PAD << REF_ELEMENT_SHIFT表示BUFFER_PAD个引用的占用字节数
         *  比如一个引用占用字节数是4，那么REF_ELEMENT_SHIFT是2
         *  BUFFER_PAD << REF_ELEMENT_SHIFT就相当于BUFFER_PAD*4
         */
        REF_ARRAY_BASE = UNSAFE.arrayBaseOffset(Object[].class) + (BUFFER_PAD << REF_ELEMENT_SHIFT);
    }

    private final long indexMask;
    private final Object[] entries;
    protected final int bufferSize;
    protected final Sequencer sequencer;

    RingBufferFields(
            EventFactory<E> eventFactory,
            Sequencer sequencer)
    {
        this.sequencer = sequencer;
        this.bufferSize = sequencer.getBufferSize();

        if (bufferSize < 1)
        {
            throw new IllegalArgumentException("bufferSize must not be less than 1");
        }
        if (Integer.bitCount(bufferSize) != 1)
        {
            throw new IllegalArgumentException("bufferSize must be a power of 2");
        }
        //数组的最大下标
        this.indexMask = bufferSize - 1;
        /**
         * bufferSize再加两倍的BUFFER_PAD大小，BUFFER_PAD分别在头尾
         */
        /**
         * 结构：缓存行填充，避免频繁访问的任一entry与另一被修改的无关变量写入同一缓存行
         * --------------
         * *   数组头   * BASE
         * *   Padding  * 128字节
         * * reference1 * SCALE
         * * reference2 * SCALE
         * * reference3 * SCALE
         * ..........
         * *   Padding  * 128字节
         * --------------
         * https://blog.csdn.net/zhxdick/article/details/52065280
         */
        this.entries = new Object[sequencer.getBufferSize() + 2 * BUFFER_PAD];
        //利用eventFactory初始化RingBuffer的每个槽
        fill(eventFactory);
    }

    //填充数组
    private void fill(EventFactory<E> eventFactory)
    {
        for (int i = 0; i < bufferSize; i++)
        {
            entries[BUFFER_PAD + i] = eventFactory.newInstance();
        }
    }

    //从数组取值
    protected final E elementAt(long sequence)
    {
        return (E) UNSAFE.getObject(entries, REF_ARRAY_BASE + ((sequence & indexMask) << REF_ELEMENT_SHIFT));
    }

}

/**
 * RingBuffer保存了整个RingBuffer每个槽的Event对象，对应的field是private final Object[]
 * 这些对象只在RingBuffer初始化时被建立，之后就是修改这些对象，并不会重新建立新的对象
 * 避免他们和被修改的对象读取到同一个缓存行，避免缓存行失效重新读取
 */
public class RingBuffer<E>  extends RingBufferFields<E> implements Cursored, EventSequencer<E>, EventSink<E> {

    RingBuffer(EventFactory<E> eventFactory, Sequencer sequencer) {
        super(eventFactory, sequencer);
    }


    public static <E> RingBuffer<E> create(
            ProducerType producerType,
            EventFactory<E> factory,
            int bufferSize,
            WaitStrategy waitStrategy)
    {
        switch (producerType)
        {
            case SINGLE:
                return createSingleProducer(factory, bufferSize, waitStrategy);
            case MULTI:
                return createMultiProducer(factory, bufferSize, waitStrategy);
            default:
                throw new IllegalStateException(producerType.toString());
        }
    }

    public static <E> RingBuffer<E> createSingleProducer(
            EventFactory<E> factory,
            int bufferSize,
            WaitStrategy waitStrategy)
    {
        return null;
    }

    /**
     * 新建一个多生产者的RingBuffer，使用默认等待策略{@link BlockingWaitStrategy}
     */
    public static <E> RingBuffer<E> createMultiProducer(EventFactory<E> factory, int bufferSize)
    {
        return createMultiProducer(factory, bufferSize, new BlockingWaitStrategy());
    }

    /**
     *使用指定等待策略新建一个多生产者的RingBuffer
     * @param factory  用于在RingBuffer创建时间
     * @param bufferSize  ringBuffer里创建的元素数量
     * @param waitStrategy  用于确定如何等待新元素可用的
     * @param <E> 存储在ringBuffer的事件类型
     * @return
     */
    public static <E> RingBuffer<E> createMultiProducer(
            EventFactory<E> factory,
            int bufferSize,
            WaitStrategy waitStrategy)
    {
        MultiProducerSequencer sequencer = new MultiProducerSequencer(bufferSize, waitStrategy);

        return new RingBuffer<E>(factory, sequencer);
    }

    /**
     * 创建一个新的SequenceBarrier,事件处理器将使用它来跟踪哪些消息
     * 可从给定要跟踪的序列列表的环形缓冲区中读取
     * @param sequencesToTrack  要跟踪的附加序列
     * @return  一个将跟踪指定序列的序列屏障
     */
    public SequenceBarrier newBarrier(Sequence... sequencesToTrack)
    {
        return sequencer.newBarrier(sequencesToTrack);
    }

    public long getCursor() {
        return 0;
    }

    public E get(long sequence) {
        return elementAt(sequence);
    }

    public int getBufferSize() {
        return 0;
    }

    @Override
    public long next() {
        return 0;
    }

    @Override
    public long next(int n) {
        return 0;
    }

    @Override
    public void publish(long sequence) {

    }

    @Override
    public void publishEvent(EventTranslator<E> translator) {
        final long sequence = sequencer.next();
        translateAndPublish(translator, sequence);
    }

    @Override
    public <A> void publishEvent(EventTranslatorOneArg<E, A> translator, A arg0) {
        final long sequence = sequencer.next();
        translateAndPublish(translator, sequence, arg0);
    }

    private <A> void translateAndPublish(EventTranslatorOneArg<E, A> translator, long sequence, A arg0)
    {
        try
        {
            translator.translateTo(get(sequence), sequence, arg0);
        }
        finally
        {
            sequencer.publish(sequence);
        }
    }

    private void translateAndPublish(EventTranslator<E> translator, long sequence) {
        try {
            translator.translateTo(get(sequence), sequence);
        }finally {
            sequencer.publish(sequence);
        }
    }

    public void addGatingSequences(Sequence... gatingSequences)
    {
        sequencer.addGatingSequences(gatingSequences);
    }

    /**
     * 从这个ringBuffer中删除指定的序列。
     * @param sequence
     * @return
     */
    public boolean removeGatingSequence(Sequence sequence)
    {
        return sequencer.removeGatingSequence(sequence);
    }
}
