package com.simats.selfora.repository;

import com.simats.selfora.entity.CaregiverPracticeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaregiverPracticeSessionRepository extends JpaRepository<CaregiverPracticeSession, Long> {
    List<CaregiverPracticeSession> findByChildIdOrderByPracticeDateDesc(Long childId);
    List<CaregiverPracticeSession> findByCaregiverIdOrderByPracticeDateDesc(Long caregiverId);
    List<CaregiverPracticeSession> findByStatusOrderBySubmittedAtDesc(String status);
    List<CaregiverPracticeSession> findByChildIdAndStatus(Long childId, String status);
    boolean existsByChildIdAndHomeProgramIdAndPracticeDate(Long childId, Long homeProgramId, java.time.LocalDate practiceDate);
    java.util.Optional<CaregiverPracticeSession> findByClientSessionId(String clientSessionId);
}
