package com.example.disruptor.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

public final class ThreadHints {

    private static final MethodHandle ON_SPIN_WAIT_METHOD_HANDLE;
    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle methodHandle = null;
        try{
            methodHandle = lookup.findStatic(Thread.class, "onSpinWait", methodType(void.class));
        }catch (final Exception ignore){}
        ON_SPIN_WAIT_METHOD_HANDLE = methodHandle;
    }

    private ThreadHints() {
    }

    /**
     * 指示调用方暂时无法继续，直到其他活动的一个或多个动作的发生
     * 通过在自旋等待循环结构的每次迭代中调用此方法调用线程向运行时表明它正在忙等待。运行时可以采取措施来提高调用自旋等待循环结构的性能
     */
    public static void onSpinWait(){
        if (null != ON_SPIN_WAIT_METHOD_HANDLE) {
            try{
                ON_SPIN_WAIT_METHOD_HANDLE.invokeExact();
            }catch (final Throwable ignore){}
        }
    }
}
