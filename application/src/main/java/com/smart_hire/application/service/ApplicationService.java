package com.smart_hire.application.service;

import com.smart_hire.application.dto.ApplicationDetailResponse;
import com.smart_hire.application.dto.CreateApplicationRequest;
import com.smart_hire.application.dto.ApplicationSummaryResponse;
import com.smart_hire.application.dto.UpdateApplicationRequest;

import java.util.List;

public interface ApplicationService {

    void apply(CreateApplicationRequest request);

    ApplicationDetailResponse getApplicationById(Long id);

    List<ApplicationSummaryResponse> getApplicationsByJobId(Long jobId);

    List<ApplicationSummaryResponse> getApplicationsByCandidateId(Long candidateId);

    ApplicationDetailResponse updateApplication(Long id, UpdateApplicationRequest request);
}
