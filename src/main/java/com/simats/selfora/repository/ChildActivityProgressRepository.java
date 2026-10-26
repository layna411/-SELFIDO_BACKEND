package com.simats.selfora.repository;

import com.simats.selfora.entity.ChildActivityProgress;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChildActivityProgressRepository extends JpaRepository<ChildActivityProgress, Long> {
    List<ChildActivityProgress> findByChildId(Long childId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cap FROM ChildActivityProgress cap WHERE cap.child.id = :childId AND cap.activity.id = :activityId")
    Optional<ChildActivityProgress> findByChildIdAndActivityIdForUpdate(@Param("childId") Long childId, @Param("activityId") Long activityId);

    Optional<ChildActivityProgress> findByChildIdAndActivityId(Long childId, Long activityId);
    Optional<ChildActivityProgress> findFirstByChildIdAndIsCompletedFalseOrderByLastPracticedAtDesc(Long childId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT IGNORE INTO child_activity_progress (child_id, activity_id, last_step_number, total_steps_completed, is_completed, stars_earned, last_practiced_at) VALUES (:childId, :activityId, 1, 0, 0, 0, NOW())", nativeQuery = true)
    void insertIgnoreProgress(@Param("childId") Long childId, @Param("activityId") Long activityId);
}
