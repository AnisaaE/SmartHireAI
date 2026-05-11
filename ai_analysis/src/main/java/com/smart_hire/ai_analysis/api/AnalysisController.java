package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.AnalysisService;
import com.smart_hire.ai_analysis.service.CandidateAnalysis;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping("/report/{jobId}")
    public AnalysisResponse getReport(@PathVariable String jobId) {
        return AnalysisResponseMapper.toResponse(analysisService.getReport(jobId));
    }

    @GetMapping("/{analysisId}/candidates")
    public java.util.List<CandidateBreakdownResponse> getCandidates(@PathVariable String analysisId) {
        return analysisService.getCandidates(analysisId).stream()
                .map(CandidateBreakdownResponse::from)
                .toList();
    }

    @PutMapping("/{analysisId}")
    public AnalysisResponse updateAnalysis(
            @PathVariable String analysisId,
            @RequestBody UpdateAnalysisRequest request
    ) {
        return AnalysisResponseMapper.toResponse(
                analysisService.updateAnalysis(analysisId, request.toCommand())
        );
    }

    @PutMapping("/{analysisId}/restart")
    public AnalysisResponse restartAnalysis(@PathVariable String analysisId) {
        return AnalysisResponseMapper.toResponse(analysisService.restartAnalysis(analysisId));
    }
}
