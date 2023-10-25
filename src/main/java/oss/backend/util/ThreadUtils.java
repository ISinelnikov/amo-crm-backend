package oss.backend.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public final class ThreadUtils {
    private ThreadUtils() {
    }

    public static ScheduledExecutorService createScheduledExecutorService(int poolSize, String threadNameTemplate) {
        return Executors.newScheduledThreadPool(poolSize, getDaemonThreadFactory(threadNameTemplate));
    }

    public static ExecutorService createExecutorService(int poolSize, String threadNameTemplate) {
        return Executors.newFixedThreadPool(poolSize, getDaemonThreadFactory(threadNameTemplate));
    }

    public static ThreadFactory getDaemonThreadFactory(String prefix) {
        String nameFormat = prefix + "-%d";
        ThreadFactory backingThreadFactory = Executors.defaultThreadFactory();
        AtomicLong count = new AtomicLong(0);
        return runnable -> {
            Thread thread = backingThreadFactory.newThread(runnable);
            thread.setName(String.format(nameFormat, count.getAndIncrement()));
            thread.setDaemon(true);
            return thread;
        };
    }
}
