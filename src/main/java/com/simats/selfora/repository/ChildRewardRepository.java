package com.simats.selfora.repository;

import com.simats.selfora.entity.ChildReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChildRewardRepository extends JpaRepository<ChildReward, Long> {
    List<ChildReward> findByChildIdOrderByEarnedAtDesc(Long childId);
    boolean existsByChildIdAndBadgeType(Long childId, String badgeType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT IGNORE INTO child_rewards (child_id, badge_type, badge_title, badge_icon, earned_at) VALUES (:childId, :badgeType, :badgeTitle, :badgeIcon, NOW())", nativeQuery = true)
    void insertIgnoreReward(@Param("childId") Long childId, @Param("badgeType") String badgeType, @Param("badgeTitle") String badgeTitle, @Param("badgeIcon") String badgeIcon);
}
