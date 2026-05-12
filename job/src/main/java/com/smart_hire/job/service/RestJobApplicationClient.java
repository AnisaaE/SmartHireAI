package com.smart_hire.job.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestJobApplicationClient implements JobApplicationClient {

    private final RestClient restClient;

    public RestJobApplicationClient(
            RestClient.Builder restClientBuilder,
            @Value("${job.integrations.application-service.base-url:http://localhost:8085}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public boolean hasApplications(Long jobId) {
        ApplicationDependencyPayload payload = restClient.get()
                .uri("/api/applications/job/{jobId}/exists", jobId)
                .retrieve()
                .body(ApplicationDependencyPayload.class);
        return payload != null && payload.exists();
    }

    private record ApplicationDependencyPayload(boolean exists) {
    }
}
