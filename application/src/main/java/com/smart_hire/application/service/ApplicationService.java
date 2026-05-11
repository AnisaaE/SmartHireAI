package com.smart_hire.application.service;

import com.smart_hire.application.dto.ApplicationDetailResponse;
import com.smart_hire.application.dto.CreateApplicationRequest;

public interface ApplicationService {

    void apply(CreateApplicationRequest request);

    ApplicationDetailResponse getApplicationById(Long id);
}
