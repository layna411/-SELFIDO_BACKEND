package com.simats.selfora.repository;

import com.simats.selfora.entity.ChildReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChildRewardRepository extends JpaRepository<ChildReward, Long> {
    List<ChildReward> findByChildIdOrderByEarnedAtDesc(Long childId);
    boolean existsByChildIdAndBadgeType(Long childId, String badgeType);
}
