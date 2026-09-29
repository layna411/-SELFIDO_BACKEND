package com.simats.selfora.repository;

import com.simats.selfora.entity.ProgressSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProgressSnapshotRepository extends JpaRepository<ProgressSnapshot, Long> {
    List<ProgressSnapshot> findByChildIdAndActivityIdOrderBySnapshotDateAsc(Long childId, Long activityId);
    List<ProgressSnapshot> findByChildIdOrderBySnapshotDateAsc(Long childId);
}
