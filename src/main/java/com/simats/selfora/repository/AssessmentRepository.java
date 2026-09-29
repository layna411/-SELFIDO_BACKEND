package com.simats.selfora.repository;

import com.simats.selfora.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByChildId(Long childId);
    Optional<Assessment> findTopByChildIdAndActivityIdOrderByAssessmentDateDesc(Long childId, Long activityId);
}
