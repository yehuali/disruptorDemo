package com.example.disruptor;

/**
 * 在跟踪依赖{{@link Sequence}},协调声明访问数据结构的序列
 */
public interface Sequencer extends Cursored, Sequenced {
    //设置-1为序列起始点
    long INITIAL_CURSOR_VALUE = -1L;
}
