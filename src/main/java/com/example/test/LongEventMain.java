package com.example.test;

import com.example.disruptor.BlockingWaitStrategy;
import com.example.disruptor.EventHandler;
import com.example.disruptor.EventTranslatorOneArg;
import com.example.disruptor.dsl.Disruptor;
import com.example.disruptor.dsl.ProducerType;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;

public class LongEventMain {

    static class IntToExampleEventTranslator implements EventTranslatorOneArg<LongEvent, Integer> {

        static final IntToExampleEventTranslator INSTANCE = new IntToExampleEventTranslator();

        public void translateTo(LongEvent event, long sequence, Integer arg0) {
            event.data = arg0 ;
            System.err.println("put data "+sequence+", "+event+", "+arg0);
        }
    }

    public static void main(String[] args) {
        final int events = 20; // 必须为偶数

        // 用于事件处理(EventProcessor)的线程工厂
        ThreadFactory threadFactory =
                new ThreadFactoryBuilder()
                        .setNameFormat("disruptor-executor-%d")
                        .setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                            public void uncaughtException(Thread t, Throwable e) {
                                System.out.println("Thread " + t + "throw " + e);
                                e.printStackTrace();
                            }
                        })

                        .build();

        int bufferSize = 8;
        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(
                new LongEventFactory(),  // 用于创建环形缓冲中对象的工厂
                8,  // 环形缓冲的大小
                threadFactory,  // 用于事件处理的线程工厂
                ProducerType.MULTI, // 生产者类型，单vs多生产者
                new BlockingWaitStrategy());

        // 消费者模拟-日志处理
        EventHandler journalHandler = new EventHandler() {
            public void onEvent(Object event, long sequence, boolean endOfBatch) throws Exception {
                Thread.sleep(8);
                System.out.println(Thread.currentThread().getId() + " process journal " + event + ", seq: " + sequence);
            }
        };

        // 生产线程0
        Thread produceThread0 = new Thread(new Runnable() {
            public void run() {
                int x = 0;
                while(x++ < events / 2){
                    disruptor.publishEvent(IntToExampleEventTranslator.INSTANCE, x);
                }
            }
        });


        // 定义消费链，先并行处理日志、解码和复制，再处理结果上报
        disruptor
                .handleEventsWith(  new EventHandler[]{journalHandler});

        disruptor.start();

    }
}
