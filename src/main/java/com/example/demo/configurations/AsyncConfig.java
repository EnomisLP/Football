package com.example.demo.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "customAsyncExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Thread pool config
        executor.setCorePoolSize(20);        // Minimum threads
        executor.setMaxPoolSize(50);          // Max threads under pressure
        executor.setQueueCapacity(1000);      // Queue tasks if threads are busy
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.setKeepAliveSeconds(60);     // Kill idle threads after 60 sec

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // Propagate SecurityContext to the async threads to avoid forbidden errors
        // when accessing secured resources in async tasks.
        executor.setTaskDecorator(new SecurityContextTaskDecorator());

        executor.initialize();
        return executor;
    }

    static class SecurityContextTaskDecorator implements TaskDecorator {

        @Override
        public Runnable decorate(Runnable runnable) {
            final SecurityContext context = SecurityContextHolder.getContext();

            // Return a decorated Runnable that sets the SecurityContext before running the task
            return () -> {
                try {
                    SecurityContextHolder.setContext(context);
                    runnable.run();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            };
        }
    }
}