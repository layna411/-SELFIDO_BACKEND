package com.simats.selfora.repository;

import com.simats.selfora.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChildRepository extends JpaRepository<Child, Long> {
    @Query(value = "SELECT c.* FROM children c JOIN therapist_children tc ON c.id = tc.child_id WHERE tc.therapist_id = :therapistId AND c.is_active = true", nativeQuery = true)
    List<Child> findByTherapistId(@Param("therapistId") Long therapistId);

    @Query(value = "SELECT c.* FROM children c JOIN caregiver_children cc ON c.id = cc.child_id WHERE cc.caregiver_id = :caregiverId AND c.is_active = true", nativeQuery = true)
    List<Child> findByCaregiverId(@Param("caregiverId") Long caregiverId);
}
