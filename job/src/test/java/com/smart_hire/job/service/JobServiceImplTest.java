package com.smart_hire.job.service;

import com.smart_hire.job.dto.CreateJobRequest;
import com.smart_hire.job.dto.JobDetailResponse;
import com.smart_hire.job.dto.UpdateJobRequest;
import com.smart_hire.job.dto.UpdateJobStatusRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobAuthClient jobAuthClient;

    @Mock
    private JobApplicationClient jobApplicationClient;

    @Mock
    private JobAnalysisClient jobAnalysisClient;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    void shouldCreateDraftJob() {
        when(jobRepository.save(any(JobEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobAuthClient.getRecruiter(5L))
                .thenReturn(new JobAuthClient.RecruiterSnapshot(5L, "recruiter", "recruiter@test.com", "RECRUITER", true));

        jobService.createJob(new CreateJobRequest(5L, "Java Dev", "Build APIs", "Sofia", "FULL_TIME"));

        ArgumentCaptor<JobEntity> captor = ArgumentCaptor.forClass(JobEntity.class);
        verify(jobRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("DRAFT");
        assertThat(captor.getValue().getRecruiterId()).isEqualTo(5L);
    }

    @Test
    void shouldRejectInactiveRecruiter() {
        when(jobAuthClient.getRecruiter(5L))
                .thenReturn(new JobAuthClient.RecruiterSnapshot(5L, "recruiter", "recruiter@test.com", "RECRUITER", false));

        assertThatThrownBy(() -> jobService.createJob(new CreateJobRequest(5L, "Java Dev", "Build APIs", "Sofia", "FULL_TIME")))
                .isInstanceOf(InvalidRecruiterException.class)
                .hasMessage("Recruiter is inactive: 5");
    }

    @Test
    void shouldRejectNonRecruiterUser() {
        when(jobAuthClient.getRecruiter(5L))
                .thenReturn(new JobAuthClient.RecruiterSnapshot(5L, "candidate", "candidate@test.com", "CANDIDATE", true));

        assertThatThrownBy(() -> jobService.createJob(new CreateJobRequest(5L, "Java Dev", "Build APIs", "Sofia", "FULL_TIME")))
                .isInstanceOf(InvalidRecruiterException.class)
                .hasMessage("User is not a recruiter: 5");
    }

    @Test
    void shouldReturnJobsForRecruiter() {
        JobEntity job = jobEntity(1L, 9L, "Platform Engineer", "OPEN");
        when(jobRepository.findByRecruiterIdOrderByCreatedAtDesc(9L)).thenReturn(List.of(job));

        assertThat(jobService.getJobsByRecruiterId(9L))
                .extracting("id", "recruiterId", "title", "status")
                .containsExactly(org.assertj.core.groups.Tuple.tuple(1L, 9L, "Platform Engineer", "OPEN"));
    }

    @Test
    void shouldUpdateJobDetails() {
        JobEntity job = jobEntity(1L, 5L, "Java Dev", "DRAFT");
        job.setDescription("Old");
        job.setLocation("Sofia");
        job.setEmploymentType("FULL_TIME");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(JobEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobDetailResponse result = jobService.updateJob(1L, new UpdateJobRequest("Lead Dev", "New desc", "Remote", "CONTRACT"));

        assertThat(result.title()).isEqualTo("Lead Dev");
        assertThat(result.description()).isEqualTo("New desc");
        assertThat(result.location()).isEqualTo("Remote");
        assertThat(result.employmentType()).isEqualTo("CONTRACT");
        verify(jobAnalysisClient).invalidateByJobId(1L);
    }

    @Test
    void shouldRejectUnknownStatus() {
        JobEntity job = jobEntity(1L, 5L, "Java Dev", "DRAFT");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.updateJobStatus(1L, new UpdateJobStatusRequest("INVALID")))
                .isInstanceOf(InvalidJobStatusException.class)
                .hasMessage("Invalid job status: INVALID");
    }

    @Test
    void shouldDeleteJobWhenNoApplicationsExist() {
        JobEntity job = jobEntity(1L, 5L, "Java Dev", "DRAFT");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobApplicationClient.hasApplications(1L)).thenReturn(false);

        jobService.deleteJobById(1L);

        verify(jobRepository).delete(job);
    }

    @Test
    void shouldArchiveJobInsteadOfDeletingWhenApplicationsExistAndJobIsClosed() {
        JobEntity job = jobEntity(1L, 5L, "Java Dev", "CLOSED");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobApplicationClient.hasApplications(1L)).thenReturn(true);
        when(jobRepository.save(any(JobEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        jobService.deleteJobById(1L);

        verify(jobRepository).save(job);
        assertThat(job.getStatus()).isEqualTo("ARCHIVED");
    }

    @Test
    void shouldRejectDeletingOpenJobWhenApplicationsExist() {
        JobEntity job = jobEntity(1L, 5L, "Java Dev", "OPEN");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobApplicationClient.hasApplications(1L)).thenReturn(true);

        assertThatThrownBy(() -> jobService.deleteJobById(1L))
                .isInstanceOf(JobDeletionConflictException.class)
                .hasMessage("Job cannot be deleted while applications exist unless it is CLOSED or ARCHIVED: 1");
    }

    private JobEntity jobEntity(Long id, Long recruiterId, String title, String status) {
        JobEntity entity = new JobEntity();
        entity.setId(id);
        entity.setRecruiterId(recruiterId);
        entity.setTitle(title);
        entity.setDescription("Description");
        entity.setLocation("Remote");
        entity.setEmploymentType("FULL_TIME");
        entity.setStatus(status);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}
