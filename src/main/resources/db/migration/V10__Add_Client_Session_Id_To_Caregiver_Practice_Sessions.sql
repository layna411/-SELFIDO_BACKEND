-- Add client_session_id column to caregiver_practice_sessions table
ALTER TABLE caregiver_practice_sessions ADD COLUMN client_session_id VARCHAR(100);
