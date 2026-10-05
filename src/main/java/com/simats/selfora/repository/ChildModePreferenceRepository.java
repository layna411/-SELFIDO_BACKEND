package com.simats.selfora.repository;

import com.simats.selfora.entity.ChildModePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChildModePreferenceRepository extends JpaRepository<ChildModePreference, Long> {
    Optional<ChildModePreference> findByChildId(Long childId);
}
