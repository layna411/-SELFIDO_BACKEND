package com.simats.selfora.repository;

import com.simats.selfora.entity.PromptLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PromptLevelRepository extends JpaRepository<PromptLevel, Integer> {
    Optional<PromptLevel> findByLevelCode(String levelCode);
}
