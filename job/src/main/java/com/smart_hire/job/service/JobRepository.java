package com.smart_hire.job.service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, Long> {

    List<JobEntity> findAllByOrderByCreatedAtDesc();

    List<JobEntity> findByRecruiterIdOrderByCreatedAtDesc(Long recruiterId);
}
