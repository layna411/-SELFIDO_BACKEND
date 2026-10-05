package com.simats.selfora.repository;

import com.simats.selfora.entity.PromptFadingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromptFadingPlanRepository extends JpaRepository<PromptFadingPlan, Long> {
    List<PromptFadingPlan> findByChildIdOrderByCreatedAtDesc(Long childId);
    List<PromptFadingPlan> findByChildIdAndActivityIdOrderByCreatedAtDesc(Long childId, Long activityId);
    List<PromptFadingPlan> findByChildIdAndStepIdOrderByCreatedAtDesc(Long childId, Long stepId);
    Optional<PromptFadingPlan> findTopByChildIdAndStepIdOrderByCreatedAtDesc(Long childId, Long stepId);
    List<PromptFadingPlan> findByChildIdAndStatus(Long childId, String status);
}
