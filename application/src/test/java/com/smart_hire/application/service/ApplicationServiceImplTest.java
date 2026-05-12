package com.smart_hire.application.service;

import com.smart_hire.application.dto.ApplicationDetailResponse;
import com.smart_hire.application.dto.CreateApplicationRequest;
import com.smart_hire.application.dto.UpdateApplicationRequest;
import com.smart_hire.application.dto.UpdateApplicationStatusRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationAuthClient authClient;

    @Mock
    private ApplicationDocumentClient documentClient;

    @Mock
    private ApplicationJobClient jobClient;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Test
    void shouldCreateApplicationWhenJobIsOpenAndCvHasExtractedText() {
        when(authClient.getCandidate(34L))
                .thenReturn(new ApplicationAuthClient.CandidateSnapshot(34L, "candidate", "candidate@test.com", "CANDIDATE", true));
        when(jobClient.getJob(12L)).thenReturn(new ApplicationJobClient.JobSnapshot(12L, 5L, "Java", "Desc", "Remote", "FULL_TIME", "OPEN"));
        when(applicationRepository.existsByJobIdAndCandidateId(12L, 34L)).thenReturn(false);
        when(documentClient.getDocumentMetadata("cv-101"))
                .thenReturn(new ApplicationDocumentClient.DocumentMetadata("cv-101", "34", "CV", "CV", "ACTIVE"));
        when(documentClient.getDocumentContent("cv-101")).thenReturn("Experienced Java developer");
        when(applicationRepository.save(any(ApplicationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        applicationService.apply(new CreateApplicationRequest(12L, 34L, "cv-101"));

        ArgumentCaptor<ApplicationEntity> captor = ArgumentCaptor.forClass(ApplicationEntity.class);
        verify(applicationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("APPLIED");
        assertThat(captor.getValue().getCvDocumentId()).isEqualTo("cv-101");
    }

    @Test
    void shouldRejectApplicationWhenCvHasNoExtractedText() {
        when(authClient.getCandidate(34L))
                .thenReturn(new ApplicationAuthClient.CandidateSnapshot(34L, "candidate", "candidate@test.com", "CANDIDATE", true));
        when(jobClient.getJob(12L)).thenReturn(new ApplicationJobClient.JobSnapshot(12L, 5L, "Java", "Desc", "Remote", "FULL_TIME", "OPEN"));
        when(applicationRepository.existsByJobIdAndCandidateId(12L, 34L)).thenReturn(false);
        when(documentClient.getDocumentMetadata("cv-101"))
                .thenReturn(new ApplicationDocumentClient.DocumentMetadata("cv-101", "34", "CV", "CV", "ACTIVE"));
        when(documentClient.getDocumentContent("cv-101")).thenReturn("   ");

        assertThatThrownBy(() -> applicationService.apply(new CreateApplicationRequest(12L, 34L, "cv-101")))
                .isInstanceOf(InvalidCvDocumentException.class)
                .hasMessage("CV document has no extracted text: cv-101");
    }

    @Test
    void shouldRejectApplicationWhenJobIsClosed() {
        when(authClient.getCandidate(34L))
                .thenReturn(new ApplicationAuthClient.CandidateSnapshot(34L, "candidate", "candidate@test.com", "CANDIDATE", true));
        when(jobClient.getJob(12L)).thenReturn(new ApplicationJobClient.JobSnapshot(12L, 5L, "Java", "Desc", "Remote", "FULL_TIME", "CLOSED"));

        assertThatThrownBy(() -> applicationService.apply(new CreateApplicationRequest(12L, 34L, "cv-101")))
                .isInstanceOf(JobUnavailableException.class)
                .hasMessage("Job is not open for applications: 12");
    }

    @Test
    void shouldRejectApplicationWhenCandidateIsInactive() {
        when(authClient.getCandidate(34L))
                .thenReturn(new ApplicationAuthClient.CandidateSnapshot(34L, "candidate", "candidate@test.com", "CANDIDATE", false));

        assertThatThrownBy(() -> applicationService.apply(new CreateApplicationRequest(12L, 34L, "cv-101")))
                .isInstanceOf(InvalidCandidateException.class)
                .hasMessage("Candidate is inactive: 34");
    }

    @Test
    void shouldRejectApplicationWhenUserIsNotCandidate() {
        when(authClient.getCandidate(34L))
                .thenReturn(new ApplicationAuthClient.CandidateSnapshot(34L, "recruiter", "recruiter@test.com", "RECRUITER", true));

        assertThatThrownBy(() -> applicationService.apply(new CreateApplicationRequest(12L, 34L, "cv-101")))
                .isInstanceOf(InvalidCandidateException.class)
                .hasMessage("User is not a candidate: 34");
    }

    @Test
    void shouldUpdateApplicationStatus() {
        ApplicationEntity entity = applicationEntity(7L, 12L, 34L, "cv-101", "APPLIED");
        when(applicationRepository.findById(7L)).thenReturn(Optional.of(entity));
        when(applicationRepository.save(any(ApplicationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationDetailResponse result = applicationService.updateApplicationStatus(7L, new UpdateApplicationStatusRequest("under_review"));

        assertThat(result.status()).isEqualTo("UNDER_REVIEW");
    }

    @Test
    void shouldUpdateCvWhenReplacementDocumentIsValid() {
        ApplicationEntity entity = applicationEntity(7L, 12L, 34L, "cv-101", "APPLIED");
        when(applicationRepository.findById(7L)).thenReturn(Optional.of(entity));
        when(documentClient.getDocumentMetadata("cv-202"))
                .thenReturn(new ApplicationDocumentClient.DocumentMetadata("cv-202", "34", "CV", "New CV", "ACTIVE"));
        when(documentClient.getDocumentContent("cv-202")).thenReturn("Updated text");
        when(applicationRepository.save(any(ApplicationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationDetailResponse result = applicationService.updateApplication(7L, new UpdateApplicationRequest("cv-202"));

        assertThat(result.cvDocumentId()).isEqualTo("cv-202");
    }

    @Test
    void shouldRejectApplicationWhenDocumentIsMarkedForDeletion() {
        when(authClient.getCandidate(34L))
                .thenReturn(new ApplicationAuthClient.CandidateSnapshot(34L, "candidate", "candidate@test.com", "CANDIDATE", true));
        when(jobClient.getJob(12L)).thenReturn(new ApplicationJobClient.JobSnapshot(12L, 5L, "Java", "Desc", "Remote", "FULL_TIME", "OPEN"));
        when(applicationRepository.existsByJobIdAndCandidateId(12L, 34L)).thenReturn(false);
        when(documentClient.getDocumentMetadata("cv-101"))
                .thenReturn(new ApplicationDocumentClient.DocumentMetadata("cv-101", "34", "CV", "CV", "MARKED_FOR_DELETION"));

        assertThatThrownBy(() -> applicationService.apply(new CreateApplicationRequest(12L, 34L, "cv-101")))
                .isInstanceOf(InvalidCvDocumentException.class)
                .hasMessage("Document is not available for applications: cv-101");
    }

    private ApplicationEntity applicationEntity(Long id, Long jobId, Long candidateId, String cvDocumentId, String status) {
        ApplicationEntity entity = new ApplicationEntity();
        entity.setId(id);
        entity.setJobId(jobId);
        entity.setCandidateId(candidateId);
        entity.setCvDocumentId(cvDocumentId);
        entity.setStatus(status);
        entity.setAppliedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}
