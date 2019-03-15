package com.example.disruptor;

import com.example.disruptor.util.Util;

import java.util.Arrays;

/**
 * 在单个序列后面隐藏一组序列
 */
public class FixedSequenceGroup extends Sequence {

    private final Sequence[] sequences;

    public FixedSequenceGroup(Sequence[] sequences)
    {
        this.sequences = Arrays.copyOf(sequences, sequences.length);
    }

    @Override
    public long get() {
        return Util.getMinimumSequence(sequences);
    }
}
