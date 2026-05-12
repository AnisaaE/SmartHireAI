package com.smart_hire.ai_analysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "analysis")
public record AnalysisRuntimeProperties(
        String engine,
        String storage,
        String redisKeyPrefix,
        Duration resultTtl,
        String jobServiceBaseUrl,
        String applicationServiceBaseUrl,
        String documentServiceBaseUrl,
        String ollamaModel,
        Double ollamaTemperature
) {
}
