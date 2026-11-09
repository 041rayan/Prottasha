package com.prottasha.core;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class AppExecutors {

    private final ExecutorService disk = Executors.newSingleThreadExecutor(
            namedThreadFactory("prottasha-disk"));
    private final ExecutorService calculation = Executors.newFixedThreadPool(
            Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors())),
            namedThreadFactory("prottasha-calc"));

    public Executor disk() {
        return disk;
    }

    public Executor calculation() {
        return calculation;
    }

    private static ThreadFactory namedThreadFactory(String prefix) {
        AtomicInteger sequence = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + sequence.getAndIncrement());
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            return thread;
        };
    }
}
