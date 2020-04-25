package com.sidneysimmons.plentifulports.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Service class for managing a pool of generic threads.
 * 
 * @author Sidney Simmons
 */
@Slf4j
@Component("threadService")
public class ThreadService {

    private ExecutorService instance;

    @PostConstruct
    public void initialize() {
        log.info("Creating thread pool.");
        instance = Executors.newCachedThreadPool();
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down thread pool.");
        instance.shutdownNow();
    }

    /**
     * Execute a given runnable in a new thread.
     * 
     * @param runnable the runnable to execute
     */
    public void execute(Runnable runnable) {
        instance.execute(runnable);
    }

}
