package com.smart_hire.application.service;

import com.smart_hire.application.dto.ApplicationDetailResponse;
import com.smart_hire.application.dto.ApplicationSummaryResponse;
import com.smart_hire.application.dto.CreateApplicationRequest;
import com.smart_hire.application.dto.UpdateApplicationRequest;
import com.smart_hire.application.dto.UpdateApplicationStatusRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "APPLIED",
            "UNDER_REVIEW",
            "SHORTLISTED",
            "REJECTED",
            "HIRED",
            "WITHDRAWN"
    );
    private static final Set<String> ACTIVE_APPLICATION_STATUSES = Set.of(
            "APPLIED",
            "UNDER_REVIEW",
            "SHORTLISTED"
    );

    private final ApplicationRepository applicationRepository;
    private final ApplicationAuthClient authClient;
    private final ApplicationDocumentClient documentClient;
    private final ApplicationJobClient jobClient;

    public ApplicationServiceImpl(
            ApplicationRepository applicationRepository,
            ApplicationAuthClient authClient,
            ApplicationDocumentClient documentClient,
            ApplicationJobClient jobClient
    ) {
        this.applicationRepository = applicationRepository;
        this.authClient = authClient;
        this.documentClient = documentClient;
        this.jobClient = jobClient;
    }

    @Override
    public void apply(CreateApplicationRequest request) {
        validateCandidate(request.candidateId());
        validateOpenJob(request.jobId());
        validateNoDuplicate(request.jobId(), request.candidateId());
        validateCvDocument(request.candidateId(), request.cvDocumentId());

        Instant now = Instant.now();
        ApplicationEntity entity = new ApplicationEntity();
        entity.setJobId(request.jobId());
        entity.setCandidateId(request.candidateId());
        entity.setCvDocumentId(request.cvDocumentId());
        entity.setStatus("APPLIED");
        entity.setAppliedAt(now);
        entity.setUpdatedAt(now);

        applicationRepository.save(entity);
    }

    @Override
    public ApplicationDetailResponse getApplicationById(Long id) {
        return toDetailResponse(getExistingApplication(id));
    }

    @Override
    public List<ApplicationSummaryResponse> getApplicationsByJobId(Long jobId) {
        return applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Override
    public boolean hasApplicationsByJobId(Long jobId) {
        return applicationRepository.existsByJobId(jobId);
    }

    @Override
    public List<ApplicationSummaryResponse> getApplicationsByCandidateId(Long candidateId) {
        return applicationRepository.findByCandidateIdOrderByAppliedAtDesc(candidateId).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Override
    public boolean hasActiveApplicationsByDocumentId(String documentId) {
        return applicationRepository.existsByCvDocumentIdAndStatusIn(documentId, ACTIVE_APPLICATION_STATUSES);
    }

    @Override
    public ApplicationDetailResponse updateApplication(Long id, UpdateApplicationRequest request) {
        ApplicationEntity entity = getExistingApplication(id);
        validateCvDocument(entity.getCandidateId(), request.cvDocumentId());
        entity.setCvDocumentId(request.cvDocumentId());
        entity.setUpdatedAt(Instant.now());
        return toDetailResponse(applicationRepository.save(entity));
    }

    @Override
    public ApplicationDetailResponse updateApplicationStatus(Long id, UpdateApplicationStatusRequest request) {
        ApplicationEntity entity = getExistingApplication(id);
        entity.setStatus(normalizeStatus(request.status()));
        entity.setUpdatedAt(Instant.now());
        return toDetailResponse(applicationRepository.save(entity));
    }

    @Override
    public void deleteApplicationById(Long id) {
        applicationRepository.delete(getExistingApplication(id));
    }

    private ApplicationEntity getExistingApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    private void validateNoDuplicate(Long jobId, Long candidateId) {
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidateId)) {
            throw new DuplicateApplicationException(jobId, candidateId);
        }
    }

    private void validateOpenJob(Long jobId) {
        ApplicationJobClient.JobSnapshot job = jobClient.getJob(jobId);
        if (!"OPEN".equalsIgnoreCase(job.status())) {
            throw new JobUnavailableException(jobId);
        }
    }

    private void validateCandidate(Long candidateId) {
        ApplicationAuthClient.CandidateSnapshot candidate = authClient.getCandidate(candidateId);
        if (!candidate.active()) {
            throw new InvalidCandidateException("Candidate is inactive: " + candidateId);
        }
        if (!"CANDIDATE".equalsIgnoreCase(candidate.role())) {
            throw new InvalidCandidateException("User is not a candidate: " + candidateId);
        }
    }

    private void validateCvDocument(Long candidateId, String documentId) {
        ApplicationDocumentClient.DocumentMetadata metadata = documentClient.getDocumentMetadata(documentId);
        if (!String.valueOf(candidateId).equals(metadata.ownerId())) {
            throw new InvalidCvDocumentException("CV document does not belong to candidate: " + candidateId);
        }
        if (!"CV".equalsIgnoreCase(metadata.type())) {
            throw new InvalidCvDocumentException("Document is not a CV: " + documentId);
        }
        if (!"ACTIVE".equalsIgnoreCase(metadata.status()) && !"REPROCESSED".equalsIgnoreCase(metadata.status())) {
            throw new InvalidCvDocumentException("Document is not available for applications: " + documentId);
        }

        String documentContent = documentClient.getDocumentContent(documentId);
        if (documentContent == null || documentContent.isBlank()) {
            throw new InvalidCvDocumentException("CV document has no extracted text: " + documentId);
        }
    }

    private ApplicationSummaryResponse toSummaryResponse(ApplicationEntity entity) {
        return new ApplicationSummaryResponse(
                entity.getId(),
                entity.getJobId(),
                entity.getCandidateId(),
                entity.getStatus()
        );
    }

    private ApplicationDetailResponse toDetailResponse(ApplicationEntity entity) {
        return new ApplicationDetailResponse(
                entity.getId(),
                entity.getJobId(),
                entity.getCandidateId(),
                entity.getCvDocumentId(),
                entity.getStatus()
        );
    }

    private String normalizeStatus(String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new InvalidApplicationStatusException(status);
        }
        return normalized;
    }
}
