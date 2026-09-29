CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(30),
    profile_photo_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE children (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL,
    avatar_url VARCHAR(500),
    diagnosis_notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE therapist_children (
    therapist_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (therapist_id, child_id),
    CONSTRAINT fk_tc_therapist FOREIGN KEY (therapist_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_tc_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);

CREATE TABLE caregiver_children (
    caregiver_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    relationship VARCHAR(50) DEFAULT 'PARENT',
    PRIMARY KEY (caregiver_id, child_id),
    CONSTRAINT fk_cc_caregiver FOREIGN KEY (caregiver_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cc_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);

CREATE TABLE activity_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sub_category VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    target_gender VARCHAR(20) DEFAULT 'ALL',
    description TEXT,
    icon_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_act_category FOREIGN KEY (category_id) REFERENCES activity_categories(id)
);

CREATE TABLE task_steps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    step_number INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    instruction_text TEXT NOT NULL,
    child_instruction TEXT NOT NULL,
    audio_prompt_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_step_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    CONSTRAINT uq_activity_step UNIQUE (activity_id, step_number)
);

CREATE TABLE micro_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    step_id BIGINT,
    name VARCHAR(150) NOT NULL,
    sequence_order INT NOT NULL,
    description TEXT,
    CONSTRAINT fk_micro_step FOREIGN KEY (step_id) REFERENCES task_steps(id) ON DELETE CASCADE
);

CREATE TABLE step_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    step_id BIGINT NOT NULL,
    media_type VARCHAR(20) NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    caption VARCHAR(255),
    CONSTRAINT fk_media_step FOREIGN KEY (step_id) REFERENCES task_steps(id) ON DELETE CASCADE
);

CREATE TABLE prompt_levels (
    id INT PRIMARY KEY,
    level_code VARCHAR(50) NOT NULL UNIQUE,
    level_name VARCHAR(100) NOT NULL,
    description TEXT,
    hierarchy_order INT NOT NULL
);

CREATE TABLE assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    therapist_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    assessment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    CONSTRAINT fk_ass_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_ass_therapist FOREIGN KEY (therapist_id) REFERENCES users(id),
    CONSTRAINT fk_ass_activity FOREIGN KEY (activity_id) REFERENCES activities(id)
);

CREATE TABLE assessment_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    step_id BIGINT NOT NULL,
    baseline_prompt_level_id INT NOT NULL,
    outcome VARCHAR(30) NOT NULL,
    notes TEXT,
    CONSTRAINT fk_ar_assessment FOREIGN KEY (assessment_id) REFERENCES assessments(id) ON DELETE CASCADE,
    CONSTRAINT fk_ar_step FOREIGN KEY (step_id) REFERENCES task_steps(id),
    CONSTRAINT fk_ar_prompt FOREIGN KEY (baseline_prompt_level_id) REFERENCES prompt_levels(id)
);

CREATE TABLE therapy_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_code VARCHAR(100) NOT NULL UNIQUE,
    child_id BIGINT NOT NULL,
    conducted_by_user_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    session_type VARCHAR(30) NOT NULL,
    environment VARCHAR(50) DEFAULT 'CLINIC',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    total_duration_seconds INT DEFAULT 0,
    summary_notes TEXT,
    CONSTRAINT fk_ts_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_ts_user FOREIGN KEY (conducted_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_ts_activity FOREIGN KEY (activity_id) REFERENCES activities(id)
);

CREATE TABLE performance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    step_id BIGINT NOT NULL,
    prompt_level_id INT NOT NULL,
    outcome VARCHAR(30) NOT NULL,
    attempts INT DEFAULT 1,
    duration_seconds INT DEFAULT 0,
    therapist_note TEXT,
    caregiver_note TEXT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pr_session FOREIGN KEY (session_id) REFERENCES therapy_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_pr_step FOREIGN KEY (step_id) REFERENCES task_steps(id),
    CONSTRAINT fk_pr_prompt FOREIGN KEY (prompt_level_id) REFERENCES prompt_levels(id)
);

CREATE TABLE adaptive_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    therapist_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    current_prompt_level_id INT NOT NULL,
    target_prompt_level_id INT NOT NULL,
    target_step_ids JSON NOT NULL,
    clinical_rationale TEXT,
    is_confirmed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ap_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_ap_therapist FOREIGN KEY (therapist_id) REFERENCES users(id),
    CONSTRAINT fk_ap_activity FOREIGN KEY (activity_id) REFERENCES activities(id),
    CONSTRAINT fk_ap_session FOREIGN KEY (session_id) REFERENCES therapy_sessions(id),
    CONSTRAINT fk_ap_cur_prompt FOREIGN KEY (current_prompt_level_id) REFERENCES prompt_levels(id),
    CONSTRAINT fk_ap_tgt_prompt FOREIGN KEY (target_prompt_level_id) REFERENCES prompt_levels(id)
);

CREATE TABLE home_programs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    therapist_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    frequency_per_week INT NOT NULL DEFAULT 3,
    target_duration_minutes INT DEFAULT 15,
    target_prompt_level_id INT NOT NULL,
    goal_statement TEXT,
    caregiver_instructions TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hp_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_hp_therapist FOREIGN KEY (therapist_id) REFERENCES users(id),
    CONSTRAINT fk_hp_activity FOREIGN KEY (activity_id) REFERENCES activities(id),
    CONSTRAINT fk_hp_prompt FOREIGN KEY (target_prompt_level_id) REFERENCES prompt_levels(id)
);

CREATE TABLE home_program_steps (
    home_program_id BIGINT NOT NULL,
    step_id BIGINT NOT NULL,
    PRIMARY KEY (home_program_id, step_id),
    CONSTRAINT fk_hps_program FOREIGN KEY (home_program_id) REFERENCES home_programs(id) ON DELETE CASCADE,
    CONSTRAINT fk_hps_step FOREIGN KEY (step_id) REFERENCES task_steps(id)
);

CREATE TABLE generalization_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    performance_record_id BIGINT NOT NULL,
    environment VARCHAR(50) NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gen_performance FOREIGN KEY (performance_record_id) REFERENCES performance_records(id) ON DELETE CASCADE
);

CREATE TABLE progress_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    independence_percentage DOUBLE NOT NULL,
    prompt_distribution_json JSON NOT NULL,
    steps_total INT NOT NULL,
    steps_independent INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ps_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_ps_activity FOREIGN KEY (activity_id) REFERENCES activities(id)
);

CREATE TABLE rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    icon_url VARCHAR(500) NOT NULL,
    stars_required INT NOT NULL DEFAULT 5,
    description VARCHAR(255)
);

CREATE TABLE child_rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cr_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_cr_reward FOREIGN KEY (reward_id) REFERENCES rewards(id)
);

CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_msg_receiver FOREIGN KEY (receiver_id) REFERENCES users(id),
    CONSTRAINT fk_msg_child FOREIGN KEY (child_id) REFERENCES children(id)
);

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    body TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
