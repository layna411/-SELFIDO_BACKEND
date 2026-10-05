package com.simats.selfora.repository;

import com.simats.selfora.entity.PromptFadingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptFadingHistoryRepository extends JpaRepository<PromptFadingHistory, Long> {
    List<PromptFadingHistory> findByChildIdOrderByChangeDateDesc(Long childId);
    List<PromptFadingHistory> findByChildIdAndStepIdOrderByChangeDateDesc(Long childId, Long stepId);
}
