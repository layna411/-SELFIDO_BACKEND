package com.simats.selfora.repository;

import com.simats.selfora.entity.CaregiverPracticeStepResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaregiverPracticeStepResultRepository extends JpaRepository<CaregiverPracticeStepResult, Long> {
    List<CaregiverPracticeStepResult> findByPracticeSessionId(Long practiceSessionId);
}
