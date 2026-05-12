package com.smart_hire.application.service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    List<ApplicationEntity> findByJobIdOrderByAppliedAtDesc(Long jobId);

    List<ApplicationEntity> findByCandidateIdOrderByAppliedAtDesc(Long candidateId);

    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    boolean existsByJobId(Long jobId);

    boolean existsByCvDocumentIdAndStatusIn(String cvDocumentId, Set<String> statuses);
}
