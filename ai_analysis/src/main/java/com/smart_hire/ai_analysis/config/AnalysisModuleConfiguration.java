package com.smart_hire.ai_analysis.config;

import com.smart_hire.ai_analysis.service.AnalysisRepository;
import com.smart_hire.ai_analysis.service.AnalysisScoringEngine;
import com.smart_hire.ai_analysis.service.AnalysisService;
import com.smart_hire.ai_analysis.service.AnalysisServiceImpl;
import com.smart_hire.ai_analysis.service.HeuristicAnalysisScoringEngine;
import com.smart_hire.ai_analysis.service.InMemoryAnalysisRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class AnalysisModuleConfiguration {

    @Bean
    AnalysisRepository analysisRepository() {
        return new InMemoryAnalysisRepository();
    }

    @Bean
    AnalysisScoringEngine analysisScoringEngine() {
        return new HeuristicAnalysisScoringEngine();
    }

    @Bean
    AnalysisService analysisService(AnalysisRepository repository, AnalysisScoringEngine scoringEngine) {
        return new AnalysisServiceImpl(repository, scoringEngine, Instant::now);
    }
}
