package com.simats.selfora.repository;

import com.simats.selfora.entity.PerformanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PerformanceRecordRepository extends JpaRepository<PerformanceRecord, Long> {
    List<PerformanceRecord> findBySessionId(Long sessionId);

    @Query(value = "SELECT pr.* FROM performance_records pr JOIN therapy_sessions ts ON pr.session_id = ts.id WHERE ts.child_id = :childId ORDER BY pr.recorded_at DESC", nativeQuery = true)
    List<PerformanceRecord> findByChildId(@Param("childId") Long childId);

    @Query(value = "SELECT pr.* FROM performance_records pr JOIN therapy_sessions ts ON pr.session_id = ts.id WHERE ts.child_id = :childId AND pr.step_id = :stepId ORDER BY pr.recorded_at DESC", nativeQuery = true)
    List<PerformanceRecord> findHistoryByChildAndStep(@Param("childId") Long childId, @Param("stepId") Long stepId);
}
