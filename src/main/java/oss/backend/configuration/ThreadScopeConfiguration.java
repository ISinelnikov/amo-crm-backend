package oss.backend.configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static oss.backend.util.ThreadUtils.createExecutorService;
import static oss.backend.util.ThreadUtils.createScheduledExecutorService;

@Configuration
public class ThreadScopeConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ThreadScopeConfiguration.class);

    public static final String DEFAULT_EXECUTOR = "defaultExecutor";
    public static final String DEFAULT_SCHEDULER = "defaultScheduler";


    @Bean(DEFAULT_EXECUTOR)
    public ExecutorService getDefaultExecutorService(@Value("${executor.default.poolSize:5}") int poolSize) {
        logger.info("Create default executor service with pool size: {}.", poolSize);
        return createExecutorService(poolSize, DEFAULT_EXECUTOR);
    }

    @Bean(DEFAULT_SCHEDULER)
    public ScheduledExecutorService getDefaultScheduledExecutorService(@Value("${scheduled.default.poolSize:5}") int poolSize) {
        logger.info("Create default scheduled executor service with pool size: {}.", poolSize);
        return createScheduledExecutorService(poolSize, DEFAULT_SCHEDULER);
    }
}