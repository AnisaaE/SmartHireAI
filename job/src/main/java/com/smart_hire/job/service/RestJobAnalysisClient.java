package com.smart_hire.job.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestJobAnalysisClient implements JobAnalysisClient {

    private final RestClient restClient;

    public RestJobAnalysisClient(
            RestClient.Builder restClientBuilder,
            @Value("${job.integrations.ai-analysis-service.base-url:http://localhost:8083}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public void invalidateByJobId(Long jobId) {
        restClient.put()
                .uri("/api/analysis/invalidate/job/{jobId}", jobId)
                .retrieve()
                .toBodilessEntity();
    }
}
