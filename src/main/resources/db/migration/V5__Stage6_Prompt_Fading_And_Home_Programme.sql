-- Stage 5 & 6: Prompt Fading, Home Programme Management & Caregiver Practice Schema Migration V5

CREATE TABLE IF NOT EXISTS prompt_fading_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    therapist_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    step_id BIGINT NOT NULL,
    current_prompt_level_id INT NOT NULL,
    target_prompt_level_id INT NOT NULL,
    clinical_reason TEXT,
    review_criteria TEXT,
    notes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'SUGGESTED',
    approved_by_therapist_id BIGINT,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pfp_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_pfp_therapist FOREIGN KEY (therapist_id) REFERENCES users(id),
    CONSTRAINT fk_pfp_activity FOREIGN KEY (activity_id) REFERENCES activities(id),
    CONSTRAINT fk_pfp_step FOREIGN KEY (step_id) REFERENCES task_steps(id),
    CONSTRAINT fk_pfp_cur_prompt FOREIGN KEY (current_prompt_level_id) REFERENCES prompt_levels(id),
    CONSTRAINT fk_pfp_tgt_prompt FOREIGN KEY (target_prompt_level_id) REFERENCES prompt_levels(id),
    CONSTRAINT fk_pfp_app_therapist FOREIGN KEY (approved_by_therapist_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS prompt_fading_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    step_id BIGINT NOT NULL,
    approved_by_therapist_id BIGINT NOT NULL,
    previous_prompt_level_id INT NOT NULL,
    new_prompt_level_id INT NOT NULL,
    change_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason TEXT,
    subsequent_performance_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pfh_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_pfh_step FOREIGN KEY (step_id) REFERENCES task_steps(id),
    CONSTRAINT fk_pfh_therapist FOREIGN KEY (approved_by_therapist_id) REFERENCES users(id),
    CONSTRAINT fk_pfh_prev_prompt FOREIGN KEY (previous_prompt_level_id) REFERENCES prompt_levels(id),
    CONSTRAINT fk_pfh_new_prompt FOREIGN KEY (new_prompt_level_id) REFERENCES prompt_levels(id)
);

DROP PROCEDURE IF EXISTS AddHomeProgramsColumns;

-- flyway:delimiter //
CREATE PROCEDURE AddHomeProgramsColumns()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_programs' AND COLUMN_NAME = 'title') THEN
        ALTER TABLE home_programs ADD COLUMN title VARCHAR(150);
    END IF;
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_programs' AND COLUMN_NAME = 'description') THEN
        ALTER TABLE home_programs ADD COLUMN description TEXT;
    END IF;
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_programs' AND COLUMN_NAME = 'status') THEN
        ALTER TABLE home_programs ADD COLUMN status VARCHAR(30) DEFAULT 'ASSIGNED';
    END IF;
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_programs' AND COLUMN_NAME = 'priority') THEN
        ALTER TABLE home_programs ADD COLUMN priority VARCHAR(20) DEFAULT 'HIGH';
    END IF;
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_programs' AND COLUMN_NAME = 'schedule_type') THEN
        ALTER TABLE home_programs ADD COLUMN schedule_type VARCHAR(30) DEFAULT 'DAILY';
    END IF;
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_programs' AND COLUMN_NAME = 'preferred_time') THEN
        ALTER TABLE home_programs ADD COLUMN preferred_time VARCHAR(30) DEFAULT 'MORNING';
    END IF;
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_programs' AND COLUMN_NAME = 'step_specific_instructions') THEN
        ALTER TABLE home_programs ADD COLUMN step_specific_instructions TEXT;
    END IF;
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_programs' AND COLUMN_NAME = 'safety_considerations') THEN
        ALTER TABLE home_programs ADD COLUMN safety_considerations TEXT;
    END IF;
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_programs' AND COLUMN_NAME = 'things_to_avoid') THEN
        ALTER TABLE home_programs ADD COLUMN things_to_avoid TEXT;
    END IF;
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_programs' AND COLUMN_NAME = 'encouragement_suggestions') THEN
        ALTER TABLE home_programs ADD COLUMN encouragement_suggestions TEXT;
    END IF;
END //
-- flyway:delimiter ;

CALL AddHomeProgramsColumns();
DROP PROCEDURE IF EXISTS AddHomeProgramsColumns;

CREATE TABLE IF NOT EXISTS caregiver_practice_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    home_program_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    caregiver_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    practice_date DATE NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 15,
    caregiver_notes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewing_therapist_id BIGINT,
    reviewed_at TIMESTAMP NULL,
    therapist_feedback TEXT,
    flagged_for_followup BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cps_program FOREIGN KEY (home_program_id) REFERENCES home_programs(id) ON DELETE CASCADE,
    CONSTRAINT fk_cps_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_cps_caregiver FOREIGN KEY (caregiver_id) REFERENCES users(id),
    CONSTRAINT fk_cps_activity FOREIGN KEY (activity_id) REFERENCES activities(id),
    CONSTRAINT fk_cps_therapist FOREIGN KEY (reviewing_therapist_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS caregiver_practice_step_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    practice_session_id BIGINT NOT NULL,
    step_id BIGINT NOT NULL,
    outcome VARCHAR(50) NOT NULL,
    observation TEXT,
    CONSTRAINT fk_cpsr_session FOREIGN KEY (practice_session_id) REFERENCES caregiver_practice_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_cpsr_step FOREIGN KEY (step_id) REFERENCES task_steps(id)
);

CREATE TABLE IF NOT EXISTS programme_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    reference_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pn_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
