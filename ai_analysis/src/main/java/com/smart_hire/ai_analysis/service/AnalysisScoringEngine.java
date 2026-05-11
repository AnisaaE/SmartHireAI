package com.smart_hire.ai_analysis.service;

import java.util.List;

public interface AnalysisScoringEngine {

    List<CandidateAnalysis> analyze(StartAnalysisCommand request);
}
