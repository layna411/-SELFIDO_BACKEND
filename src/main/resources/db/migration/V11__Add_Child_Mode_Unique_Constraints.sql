-- Migration V11: Enforce Unique Constraints on Child Activity Progress & Child Rewards for Concurrency Control

-- 1. Deduplicate child_activity_progress keeping the row with highest ID
DELETE cap1 FROM child_activity_progress cap1
INNER JOIN child_activity_progress cap2 
ON cap1.child_id = cap2.child_id AND cap1.activity_id = cap2.activity_id
WHERE cap1.id < cap2.id;

-- 2. Add Unique Constraint on (child_id, activity_id)
ALTER TABLE child_activity_progress 
ADD CONSTRAINT uk_cap_child_activity UNIQUE (child_id, activity_id);

-- 3. Deduplicate child_rewards keeping the row with highest ID
DELETE cr1 FROM child_rewards cr1
INNER JOIN child_rewards cr2 
ON cr1.child_id = cr2.child_id AND cr1.badge_type = cr2.badge_type
WHERE cr1.id < cr2.id;

-- 4. Add Unique Constraint on (child_id, badge_type)
ALTER TABLE child_rewards 
ADD CONSTRAINT uk_cr_child_badge UNIQUE (child_id, badge_type);
