-- =========================================================================
-- Flyway Migration V19: Add Therapist Professional Profile Fields to Users Table
-- =========================================================================

ALTER TABLE users 
    ADD COLUMN designation VARCHAR(150) NULL,
    ADD COLUMN department VARCHAR(150) NULL,
    ADD COLUMN license_id VARCHAR(100) NULL,
    ADD COLUMN specialization VARCHAR(255) NULL,
    ADD COLUMN experience VARCHAR(100) NULL,
    ADD COLUMN qualification VARCHAR(150) NULL;
