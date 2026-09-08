package com.architect.backend.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Asynchronous execution සඳහා Java 21 Virtual Threads (Project Loom) configure කිරීම.
 * සම්ප්‍රදායික Platform Threads වෙනුවට Virtual Threads භාවිතා කිරීමෙන් memory consumption එක
 * අවම කර එකවර analysis jobs දහස් ගණනක් thread pool starvation එකකින් තොරව ධාවනය කළ හැක (SRS NFR).
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
