package com.smart_hire.job.service;

import com.smart_hire.job.dto.CreateJobRequest;
import com.smart_hire.job.dto.JobDetailResponse;
import com.smart_hire.job.dto.JobSummaryResponse;
import com.smart_hire.job.dto.UpdateJobRequest;
import com.smart_hire.job.dto.UpdateJobStatusRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class JobServiceImpl implements JobService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "OPEN", "CLOSED", "ARCHIVED");

    private final JobRepository jobRepository;
    private final JobAuthClient jobAuthClient;
    private final JobApplicationClient jobApplicationClient;
    private final JobAnalysisClient jobAnalysisClient;

    public JobServiceImpl(
            JobRepository jobRepository,
            JobAuthClient jobAuthClient,
            JobApplicationClient jobApplicationClient,
            JobAnalysisClient jobAnalysisClient
    ) {
        this.jobRepository = jobRepository;
        this.jobAuthClient = jobAuthClient;
        this.jobApplicationClient = jobApplicationClient;
        this.jobAnalysisClient = jobAnalysisClient;
    }

    @Override
    public void createJob(CreateJobRequest request) {
        validateRecruiter(request.recruiterId());
        Instant now = Instant.now();

        JobEntity entity = new JobEntity();
        entity.setRecruiterId(request.recruiterId());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setLocation(request.location());
        entity.setEmploymentType(request.employmentType());
        entity.setStatus("DRAFT");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        jobRepository.save(entity);
    }

    private void validateRecruiter(Long recruiterId) {
        JobAuthClient.RecruiterSnapshot recruiter = jobAuthClient.getRecruiter(recruiterId);
        if (!recruiter.active()) {
            throw new InvalidRecruiterException("Recruiter is inactive: " + recruiterId);
        }
        if (!"RECRUITER".equalsIgnoreCase(recruiter.role())) {
            throw new InvalidRecruiterException("User is not a recruiter: " + recruiterId);
        }
    }

    @Override
    public List<JobSummaryResponse> getAllJobs() {
        return jobRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Override
    public JobDetailResponse getJobById(Long id) {
        return toDetailResponse(getExistingJob(id));
    }

    @Override
    public List<JobSummaryResponse> getJobsByRecruiterId(Long recruiterId) {
        return jobRepository.findByRecruiterIdOrderByCreatedAtDesc(recruiterId).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Override
    public JobDetailResponse updateJob(Long id, UpdateJobRequest request) {
        JobEntity entity = getExistingJob(id);
        boolean descriptionChanged = !entity.getDescription().equals(request.description());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setLocation(request.location());
        entity.setEmploymentType(request.employmentType());
        entity.setUpdatedAt(Instant.now());
        JobEntity updated = jobRepository.save(entity);
        if (descriptionChanged) {
            jobAnalysisClient.invalidateByJobId(updated.getId());
        }
        return toDetailResponse(updated);
    }

    @Override
    public JobDetailResponse updateJobStatus(Long id, UpdateJobStatusRequest request) {
        JobEntity entity = getExistingJob(id);
        entity.setStatus(normalizeStatus(request.status()));
        entity.setUpdatedAt(Instant.now());
        return toDetailResponse(jobRepository.save(entity));
    }

    @Override
    public void deleteJobById(Long id) {
        JobEntity entity = getExistingJob(id);
        if (!jobApplicationClient.hasApplications(id)) {
            jobRepository.delete(entity);
            return;
        }
        if (!"CLOSED".equalsIgnoreCase(entity.getStatus()) && !"ARCHIVED".equalsIgnoreCase(entity.getStatus())) {
            throw new JobDeletionConflictException(id);
        }
        entity.setStatus("ARCHIVED");
        entity.setUpdatedAt(Instant.now());
        jobRepository.save(entity);
    }

    private JobEntity getExistingJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
    }

    private JobSummaryResponse toSummaryResponse(JobEntity entity) {
        return new JobSummaryResponse(
                entity.getId(),
                entity.getRecruiterId(),
                entity.getTitle(),
                entity.getStatus()
        );
    }

    private JobDetailResponse toDetailResponse(JobEntity entity) {
        return new JobDetailResponse(
                entity.getId(),
                entity.getRecruiterId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getLocation(),
                entity.getEmploymentType(),
                entity.getStatus()
        );
    }

    private String normalizeStatus(String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new InvalidJobStatusException(status);
        }
        return normalized;
    }
}
