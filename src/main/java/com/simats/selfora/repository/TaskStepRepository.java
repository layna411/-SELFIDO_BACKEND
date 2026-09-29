package com.simats.selfora.repository;

import com.simats.selfora.entity.TaskStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskStepRepository extends JpaRepository<TaskStep, Long> {
    List<TaskStep> findByActivityIdOrderByStepNumberAsc(Long activityId);
}
