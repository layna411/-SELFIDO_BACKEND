-- =========================================================================
-- Standalone SQL Script: Clean All Children Data & Therapist Records
-- Retention Policy: Keep ONLY Admin Credentials (ROLE_SUPER_ADMIN)
-- Database: selfora_db
-- =========================================================================

USE selfora_db;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Delete child progress, clinical sessions, and activity performance records
DELETE FROM performance_records;
DELETE FROM therapy_sessions;
DELETE FROM generalization_records;
DELETE FROM progress_snapshots;
DELETE FROM assessment_results;
DELETE FROM assessments;
DELETE FROM adaptive_plans;
DELETE FROM prompt_fading_histories;
DELETE FROM prompt_fading_plans;
DELETE FROM caregiver_practice_step_results;
DELETE FROM caregiver_practice_sessions;
DELETE FROM home_program_steps;
DELETE FROM home_programs;
DELETE FROM child_activity_progress;
DELETE FROM child_rewards;
DELETE FROM child_mode_preferences;

-- 2. Delete messages and notification logs
DELETE FROM messages;
DELETE FROM notifications;
DELETE FROM programme_notifications;

-- 3. Delete therapist-child and caregiver-child linkings
DELETE FROM therapist_children;
DELETE FROM caregiver_children;

-- 4. Delete all children records
DELETE FROM children;

-- Reset Auto-Increment IDs
ALTER TABLE children AUTO_INCREMENT = 1;
ALTER TABLE therapy_sessions AUTO_INCREMENT = 1;
ALTER TABLE performance_records AUTO_INCREMENT = 1;
ALTER TABLE assessments AUTO_INCREMENT = 1;
ALTER TABLE assessment_results AUTO_INCREMENT = 1;
ALTER TABLE home_programs AUTO_INCREMENT = 1;
ALTER TABLE home_program_steps AUTO_INCREMENT = 1;
ALTER TABLE caregiver_practice_sessions AUTO_INCREMENT = 1;
ALTER TABLE caregiver_practice_step_results AUTO_INCREMENT = 1;
ALTER TABLE adaptive_plans AUTO_INCREMENT = 1;
ALTER TABLE prompt_fading_plans AUTO_INCREMENT = 1;
ALTER TABLE prompt_fading_histories AUTO_INCREMENT = 1;

-- 5. Delete all non-admin user roles and users (keep only Admin credentials)
DELETE ur FROM user_roles ur
JOIN users u ON ur.user_id = u.id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username != 'admin' AND (r.name IS NULL OR r.name NOT IN ('ROLE_SUPER_ADMIN', 'ROLE_ADMIN'));

DELETE FROM users 
WHERE username != 'admin'
AND id NOT IN (
    SELECT ur.user_id FROM user_roles ur 
    JOIN roles r ON ur.role_id = r.id 
    WHERE r.name IN ('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')
);

-- 6. Ensure Super Admin role and user exist with active credentials
INSERT INTO roles (id, name, description)
SELECT 4, 'ROLE_SUPER_ADMIN', 'System Super Administrator with full administrative access'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SUPER_ADMIN');

INSERT INTO users (id, username, password_hash, full_name, email, phone, is_active, must_change_password)
VALUES (4, 'admin', '$2a$10$EblZqNptyYvcLm/VwDCVAu.zBkWuF.59z1N7q/4V40vL2WvO4N0.K', 'System Administrator', 'admin@selfora.org', '+919999999999', TRUE, FALSE)
ON DUPLICATE KEY UPDATE 
    password_hash = '$2a$10$EblZqNptyYvcLm/VwDCVAu.zBkWuF.59z1N7q/4V40vL2WvO4N0.K',
    is_active = TRUE,
    must_change_password = FALSE;

-- Ensure admin has ROLE_SUPER_ADMIN
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_SUPER_ADMIN';

-- Clear audit logs
DELETE FROM audit_logs;

SET FOREIGN_KEY_CHECKS = 1;

-- Verification Queries
SELECT 'Admin User Retained:' AS status, id, username, email, is_active FROM users WHERE username = 'admin';
SELECT 'Remaining Children Count:' AS status, COUNT(*) AS count FROM children;
SELECT 'Remaining Non-Admin Users Count:' AS status, COUNT(*) AS count FROM users WHERE username != 'admin';
