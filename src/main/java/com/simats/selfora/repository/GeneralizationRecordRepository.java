package com.simats.selfora.repository;

import com.simats.selfora.entity.GeneralizationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GeneralizationRecordRepository extends JpaRepository<GeneralizationRecord, Long> {
    List<GeneralizationRecord> findByPerformanceRecordId(Long performanceRecordId);
    List<GeneralizationRecord> findByEnvironment(String environment);
}
