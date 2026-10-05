-- Stage 7: Child Mode Enhancement Schema Migration V6

CREATE TABLE IF NOT EXISTS child_activity_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    last_step_number INT DEFAULT 1,
    total_steps_completed INT DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    stars_earned INT DEFAULT 0,
    last_practiced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cap_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,
    CONSTRAINT fk_cap_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS child_rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    badge_type VARCHAR(50) NOT NULL,
    badge_title VARCHAR(100) NOT NULL,
    badge_icon VARCHAR(100),
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cr_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS child_mode_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL UNIQUE,
    preferred_name VARCHAR(100),
    audio_enabled BOOLEAN DEFAULT TRUE,
    animation_enabled BOOLEAN DEFAULT TRUE,
    reduced_motion BOOLEAN DEFAULT FALSE,
    language VARCHAR(20) DEFAULT 'EN',
    reward_sounds BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cmp_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);
