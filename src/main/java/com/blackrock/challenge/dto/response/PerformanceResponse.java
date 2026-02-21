package com.blackrock.challenge.dto.response;

public record PerformanceResponse(
        String uptime,
        long memoryUsedMB,
        long totalMemoryMB,
        long maxMemoryMB,
        int activeThreads
) {}