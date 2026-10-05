-- Flyway Migration V9: Super Admin Role, Initial Admin User, and Audit Logs Table

-- Insert ROLE_SUPER_ADMIN if it doesn't already exist
INSERT INTO roles (id, name, description)
SELECT 4, 'ROLE_SUPER_ADMIN', 'System Super Administrator with full administrative access'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SUPER_ADMIN');

-- Insert Initial Super Admin User (username: admin, password: password123, must_change_password: true)
INSERT INTO users (id, username, password_hash, full_name, email, phone, is_active, must_change_password)
SELECT 4, 'admin', '$2a$10$EblZqNptyYvcLm/VwDCVAu.zBkWuF.59z1N7q/4V40vL2WvO4N0.K', 'System Administrator', 'admin@selfora.org', '+919999999999', TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Assign ROLE_SUPER_ADMIN to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_SUPER_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT,
    actor_role VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    affected_entity VARCHAR(100),
    change_summary TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_al_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Index for efficient audit searching
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
