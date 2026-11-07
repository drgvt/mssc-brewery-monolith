/*
 * Этот класс включает в приложении асинхронность (@EnableAsync)
 * и планировщик задач (@EnableScheduling), а также создает
 * простой пул потоков (SimpleAsyncTaskExecutor), чтобы задачи (например,
 * регулярная проверка склада) могли выполняться в фоне, не блокируя основной поток.
 */

package guru.sfg.brewery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Task Configuration - enable asyc tasks
 */
@EnableScheduling
@EnableAsync
@Configuration
public class TaskConfig {
    @Bean
    TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
}
