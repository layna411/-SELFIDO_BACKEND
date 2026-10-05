package com.simats.selfora.repository;

import com.simats.selfora.entity.ProgrammeNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgrammeNotificationRepository extends JpaRepository<ProgrammeNotification, Long> {
    List<ProgrammeNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<ProgrammeNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
}
