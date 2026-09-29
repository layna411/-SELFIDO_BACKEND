package com.simats.selfora.repository;

import com.simats.selfora.entity.AdaptivePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdaptivePlanRepository extends JpaRepository<AdaptivePlan, Long> {
    List<AdaptivePlan> findByChildIdOrderByCreatedAtDesc(Long childId);
    Optional<AdaptivePlan> findTopByChildIdAndActivityIdOrderByCreatedAtDesc(Long childId, Long activityId);
}
