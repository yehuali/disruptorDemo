package com.example.disruptor;

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
}
