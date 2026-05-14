package com.smart_hire.ai_analysis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smart_hire.ai_analysis.service.AnalysisRepository;
import com.smart_hire.ai_analysis.service.AnalysisScoringEngine;
import com.smart_hire.ai_analysis.service.AnalysisService;
import com.smart_hire.ai_analysis.service.AnalysisApplicationClient;
import com.smart_hire.ai_analysis.service.AnalysisJobClient;
import com.smart_hire.ai_analysis.service.AnalysisServiceImpl;
import com.smart_hire.ai_analysis.service.DocumentTextClient;
import com.smart_hire.ai_analysis.service.HeuristicAnalysisScoringEngine;
import com.smart_hire.ai_analysis.service.InMemoryAnalysisRepository;
import com.smart_hire.ai_analysis.service.LlmAnalysisResponseParser;
import com.smart_hire.ai_analysis.service.LlmAnalysisScoringEngine;
import com.smart_hire.ai_analysis.service.RedisAnalysisRepository;
import com.smart_hire.ai_analysis.service.RestAnalysisApplicationClient;
import com.smart_hire.ai_analysis.service.RestAnalysisJobClient;
import com.smart_hire.ai_analysis.service.RestDocumentTextClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Configuration
public class AnalysisModuleConfiguration {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    AnalysisRepository analysisRepository(
            AnalysisRuntimeProperties properties,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        if ("redis".equalsIgnoreCase(properties.storage())) {
            return new RedisAnalysisRepository(
                    redisTemplate,
                    objectMapper,
                    properties.redisKeyPrefix(),
                    properties.resultTtl()
            );
        }

        return new InMemoryAnalysisRepository();
    }

    @Bean
    AnalysisScoringEngine heuristicAnalysisScoringEngine() {
        return new HeuristicAnalysisScoringEngine();
    }

    @Bean
    DocumentTextClient documentTextClient(AnalysisRuntimeProperties properties) {
        return new RestDocumentTextClient(properties);
    }

    @Bean
    AnalysisJobClient analysisJobClient(AnalysisRuntimeProperties properties) {
        return new RestAnalysisJobClient(RestClient.builder(), properties.jobServiceBaseUrl());
    }

    @Bean
    AnalysisApplicationClient analysisApplicationClient(AnalysisRuntimeProperties properties) {
        return new RestAnalysisApplicationClient(RestClient.builder(), properties.applicationServiceBaseUrl());
    }

    @Bean
    LlmAnalysisResponseParser llmAnalysisResponseParser(ObjectMapper objectMapper) {
        return new LlmAnalysisResponseParser(objectMapper);
    }

    @Bean
    @Primary
    AnalysisScoringEngine analysisScoringEngine(
            AnalysisRuntimeProperties properties,
            ChatModel chatModel,
            LlmAnalysisResponseParser responseParser,
            DocumentTextClient documentTextClient,
            AnalysisScoringEngine heuristicAnalysisScoringEngine
    ) {
        if ("llm".equalsIgnoreCase(properties.engine())) {
            return new LlmAnalysisScoringEngine(
                    ChatClient.create(chatModel),
                    responseParser,
                    documentTextClient,
                    heuristicAnalysisScoringEngine,
                    properties.ollamaModel(),
                    properties.ollamaTemperature()
            );
        }

        return heuristicAnalysisScoringEngine;
    }

    @Bean
    AnalysisService analysisService(
            AnalysisRepository repository,
            AnalysisScoringEngine scoringEngine,
            AnalysisJobClient analysisJobClient,
            AnalysisApplicationClient analysisApplicationClient,
            DocumentTextClient documentTextClient
    ) {
        return new AnalysisServiceImpl(
                repository,
                scoringEngine,
                analysisJobClient,
                analysisApplicationClient,
                documentTextClient,
                Instant::now
        );
    }
}
