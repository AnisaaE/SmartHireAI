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

    public JobServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public void createJob(CreateJobRequest request) {
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
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setLocation(request.location());
        entity.setEmploymentType(request.employmentType());
        entity.setUpdatedAt(Instant.now());
        return toDetailResponse(jobRepository.save(entity));
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
        jobRepository.delete(getExistingJob(id));
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
