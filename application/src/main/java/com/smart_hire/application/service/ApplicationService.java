package com.smart_hire.application.service;

import com.smart_hire.application.dto.ApplicationDetailResponse;
import com.smart_hire.application.dto.CreateApplicationRequest;
import com.smart_hire.application.dto.ApplicationSummaryResponse;

import java.util.List;

public interface ApplicationService {

    void apply(CreateApplicationRequest request);

    ApplicationDetailResponse getApplicationById(Long id);

    List<ApplicationSummaryResponse> getApplicationsByJobId(Long jobId);
}
