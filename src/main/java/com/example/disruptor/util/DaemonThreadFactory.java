package com.example.disruptor.util;

import java.util.concurrent.ThreadFactory;

/**
 * 访问ThreadFactory实例。所有线程都是用setDaemon创建的(true)。
 */
public enum DaemonThreadFactory implements ThreadFactory {
    INSTANCE;


    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }
}
