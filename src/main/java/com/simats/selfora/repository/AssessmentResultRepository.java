package com.simats.selfora.repository;

import com.simats.selfora.entity.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Long> {
    List<AssessmentResult> findByAssessmentId(Long assessmentId);
}
