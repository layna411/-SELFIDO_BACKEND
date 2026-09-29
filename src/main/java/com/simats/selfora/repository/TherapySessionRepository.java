package com.simats.selfora.repository;

import com.simats.selfora.entity.TherapySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TherapySessionRepository extends JpaRepository<TherapySession, Long> {
    List<TherapySession> findByChildIdOrderByStartTimeDesc(Long childId);
    Optional<TherapySession> findBySessionCode(String sessionCode);
    List<TherapySession> findByChildIdAndActivityIdOrderByStartTimeDesc(Long childId, Long activityId);
}
