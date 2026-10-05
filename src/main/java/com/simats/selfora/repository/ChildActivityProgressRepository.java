package com.simats.selfora.repository;

import com.simats.selfora.entity.ChildActivityProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChildActivityProgressRepository extends JpaRepository<ChildActivityProgress, Long> {
    List<ChildActivityProgress> findByChildId(Long childId);
    Optional<ChildActivityProgress> findByChildIdAndActivityId(Long childId, Long activityId);
    Optional<ChildActivityProgress> findFirstByChildIdAndIsCompletedFalseOrderByLastPracticedAtDesc(Long childId);
}
