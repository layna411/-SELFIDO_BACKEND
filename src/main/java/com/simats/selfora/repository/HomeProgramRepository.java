package com.simats.selfora.repository;

import com.simats.selfora.entity.HomeProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HomeProgramRepository extends JpaRepository<HomeProgram, Long> {
    List<HomeProgram> findByChildIdAndIsActiveTrue(Long childId);
    List<HomeProgram> findByTherapistId(Long therapistId);
    Optional<HomeProgram> findTopByChildIdAndIsActiveTrueOrderByCreatedAtDesc(Long childId);
}
