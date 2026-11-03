-- =========================================================================
-- Flyway Migration V14: Add UNABLE and NOT_ASSESSED Prompt Levels
-- Allows foreign key consistency in performance_records and assessment_results
-- while maintaining the distinct clinical status of UNABLE and NOT_ASSESSED
-- =========================================================================

INSERT INTO prompt_levels (id, level_code, level_name, description, hierarchy_order) VALUES
(7, 'UNABLE', 'Unable', 'Child could not complete step during session', 7),
(8, 'NOT_ASSESSED', 'Not Assessed', 'Step skipped or not clinically assessed', 8)
ON DUPLICATE KEY UPDATE 
    level_name = VALUES(level_name),
    description = VALUES(description),
    hierarchy_order = VALUES(hierarchy_order);

