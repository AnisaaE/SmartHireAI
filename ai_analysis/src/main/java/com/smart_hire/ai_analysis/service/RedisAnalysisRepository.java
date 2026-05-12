package com.smart_hire.ai_analysis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

public class RedisAnalysisRepository implements AnalysisRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String keyPrefix;
    private final Duration resultTtl;

    public RedisAnalysisRepository(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            String keyPrefix,
            Duration resultTtl
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.keyPrefix = keyPrefix;
        this.resultTtl = resultTtl;
    }

    @Override
    public AnalysisResult save(AnalysisResult analysisResult) {
        writeJson(resultKey(analysisResult.analysisId()), analysisResult, resultTtl);
        redisTemplate.opsForValue().set(jobKey(analysisResult.jobId()), analysisResult.analysisId(), resultTtl);
        return analysisResult;
    }

    @Override
    public AnalysisResult findById(String analysisId) {
        return readJson(resultKey(analysisId), AnalysisResult.class);
    }

    @Override
    public AnalysisResult findByJobId(String jobId) {
        String analysisId = redisTemplate.opsForValue().get(jobKey(jobId));
        return analysisId == null ? null : findById(analysisId);
    }

    @Override
    public void saveCommand(String analysisId, StartAnalysisCommand command) {
        writeJson(commandKey(analysisId), command, resultTtl);
    }

    @Override
    public StartAnalysisCommand findCommandById(String analysisId) {
        return readJson(commandKey(analysisId), StartAnalysisCommand.class);
    }

    @Override
    public void deleteById(String analysisId) {
        AnalysisResult result = findById(analysisId);
        redisTemplate.delete(resultKey(analysisId));
        redisTemplate.delete(commandKey(analysisId));
        if (result != null) {
            String currentJobAnalysisId = redisTemplate.opsForValue().get(jobKey(result.jobId()));
            if (analysisId.equals(currentJobAnalysisId)) {
                redisTemplate.delete(jobKey(result.jobId()));
            }
        }
    }

    private <T> void writeJson(String key, T value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to write analysis payload to Redis", exception);
        }
    }

    private <T> T readJson(String key, Class<T> type) {
        String payload = redisTemplate.opsForValue().get(key);
        if (payload == null) {
            return null;
        }

        try {
            return objectMapper.readValue(payload, type);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to read analysis payload from Redis", exception);
        }
    }

    private String resultKey(String analysisId) {
        return keyPrefix + ":result:" + analysisId;
    }

    private String commandKey(String analysisId) {
        return keyPrefix + ":command:" + analysisId;
    }

    private String jobKey(String jobId) {
        return keyPrefix + ":job:" + jobId;
    }
}
