package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.AnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public AnalysisResponse startAnalysis(@RequestBody StartAnalysisRequest request) {
        return AnalysisResponseMapper.toResponse(analysisService.startAnalysis(request.toCommand()));
    }

    @GetMapping("/{analysisId}")
    public AnalysisResponse getAnalysis(@PathVariable String analysisId) {
        return AnalysisResponseMapper.toResponse(analysisService.getAnalysis(analysisId));
    }
}
