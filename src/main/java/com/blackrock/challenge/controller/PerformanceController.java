package com.blackrock.challenge.controller;

import com.blackrock.challenge.dto.response.PerformanceResponse;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class PerformanceController {

    @GetMapping("/performance")
    public PerformanceResponse performance() {

        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        long usedMemory = totalMemory - freeMemory;

        long totalMB = totalMemory / (1024 * 1024);
        long usedMB = usedMemory / (1024 * 1024);
        long maxMB = maxMemory / (1024 * 1024);

        RuntimeMXBean runtimeMXBean =
                ManagementFactory.getRuntimeMXBean();

        Duration uptime =
                Duration.ofMillis(runtimeMXBean.getUptime());

        ThreadMXBean threadBean =
                ManagementFactory.getThreadMXBean();

        int threads = threadBean.getThreadCount();

        return new PerformanceResponse(
                uptime.toString(),
                usedMB,
                totalMB,
                maxMB,
                threads
        );
    }
}