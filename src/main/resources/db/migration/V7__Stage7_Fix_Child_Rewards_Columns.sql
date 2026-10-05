-- Migration V7: Add missing badge_icon column to child_rewards table

ALTER TABLE child_rewards ADD COLUMN badge_icon VARCHAR(100);
