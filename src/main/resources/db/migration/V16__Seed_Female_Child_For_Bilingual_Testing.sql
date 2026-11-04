-- Flyway Migration V16: Seed Demo Female Child for Dual Gender Testing
INSERT INTO children (id, first_name, last_name, date_of_birth, gender, avatar_url, diagnosis_notes, is_active)
VALUES (2, 'Ananya', 'Sharma', '2020-03-22', 'FEMALE', 'https://images.unsplash.com/photo-1595454223600-91fb57997a31', 'ADL focus on upper and lower body dressing independence and eating skills.', TRUE)
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), gender = 'FEMALE', is_active = TRUE;

INSERT IGNORE INTO therapist_children (therapist_id, child_id) VALUES (1, 2);
INSERT IGNORE INTO caregiver_children (caregiver_id, child_id, relationship) VALUES (2, 2, 'MOTHER');

