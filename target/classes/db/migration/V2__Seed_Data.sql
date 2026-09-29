-- Seed Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_THERAPIST', 'Occupational Therapist managing clinical assessment and adaptation'),
(2, 'ROLE_CAREGIVER', 'Parent or caregiver carrying out home programs'),
(3, 'ROLE_CHILD', 'Child user mode with simplified interface');

-- Seed Users (BCrypt for 'password123': $2a$10$EblZqNptyYvcLm/VwDCVAu.zBkWuF.59z1N7q/4V40vL2WvO4N0.K)
INSERT INTO users (id, username, password_hash, full_name, email, phone, is_active) VALUES
(1, 'therapist1', '$2a$10$EblZqNptyYvcLm/VwDCVAu.zBkWuF.59z1N7q/4V40vL2WvO4N0.K', 'Dr. Sarah Jenkins (OT)', 'sarah.jenkins@selfora.org', '+919876543210', TRUE),
(2, 'caregiver1', '$2a$10$EblZqNptyYvcLm/VwDCVAu.zBkWuF.59z1N7q/4V40vL2WvO4N0.K', 'Priya Sharma (Parent)', 'priya.sharma@example.com', '+919876543211', TRUE),
(3, 'child1', '$2a$10$EblZqNptyYvcLm/VwDCVAu.zBkWuF.59z1N7q/4V40vL2WvO4N0.K', 'Aarav Sharma', 'aarav@example.com', '+919876543212', TRUE);

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- Therapist
(2, 2), -- Caregiver
(3, 3); -- Child

-- Seed Demo Child
INSERT INTO children (id, first_name, last_name, date_of_birth, gender, avatar_url, diagnosis_notes, is_active) VALUES
(1, 'Aarav', 'Sharma', '2019-05-15', 'MALE', 'https://images.unsplash.com/photo-1543332164-6e82f355badc', 'Occupational Therapy ADL focus on dressing independence and fine motor coordination.', TRUE);

INSERT INTO therapist_children (therapist_id, child_id) VALUES (1, 1);
INSERT INTO caregiver_children (caregiver_id, child_id, relationship) VALUES (2, 1, 'MOTHER');

-- Seed Prompt Levels
INSERT INTO prompt_levels (id, level_code, level_name, description, hierarchy_order) VALUES
(0, 'INDEPENDENT', 'Independent', 'Child completes step independently after natural instruction/cue', 0),
(1, 'VISUAL', 'Visual Prompt', 'Picture, visual cue, step photograph, or short visual sequence', 1),
(2, 'GESTURE', 'Gesture Prompt', 'Point toward sleeve/spoon/action', 2),
(3, 'VERBAL', 'Verbal Prompt', 'Short, clear spoken instruction', 3),
(4, 'MODEL_VIDEO', 'Model / Video Prompt', 'Therapist demonstration or short step video', 4),
(5, 'PARTIAL_PHYSICAL', 'Partial Physical Assistance', 'Minimal physical guidance at elbow/wrist', 5),
(6, 'FULL_PHYSICAL', 'Full Physical Assistance', 'Hand-over-hand physical assistance', 6);

-- Seed Categories
INSERT INTO activity_categories (id, name, sub_category, description) VALUES
(1, 'DRESSING', 'UPPER_BODY_BOYS', 'Upper Body Dressing Activities for Boys'),
(2, 'DRESSING', 'UPPER_BODY_GIRLS', 'Upper Body Dressing Activities for Girls'),
(3, 'DRESSING', 'LOWER_BODY_BOYS', 'Lower Body Dressing Activities for Boys'),
(4, 'DRESSING', 'LOWER_BODY_GIRLS', 'Lower Body Dressing Activities for Girls'),
(5, 'DRESSING', 'COMMON', 'Footwear and Socks ADL Skills'),
(6, 'EATING', 'COMMON', 'Independent Feeding and Utensil Usage');

-- Seed Activities
INSERT INTO activities (id, category_id, title, target_gender, description, icon_url, is_active) VALUES
(1, 1, 'T-Shirt', 'ALL', 'Putting on a standard round-neck T-shirt step by step.', 'ic_tshirt', TRUE),
(2, 1, 'Shirt', 'BOYS', 'Putting on a button-down shirt.', 'ic_shirt', TRUE),
(3, 1, 'Zip-Up Jacket', 'ALL', 'Donning and zipping a jacket.', 'ic_jacket', TRUE),
(4, 1, 'Vest', 'ALL', 'Putting on an inner vest.', 'ic_vest', TRUE),
(5, 3, 'Trousers with Drawstring', 'BOYS', 'Donning trousers and tying drawstring knot.', 'ic_trousers', TRUE),
(6, 3, 'Jeans', 'ALL', 'Donning jeans with button/zip.', 'ic_jeans', TRUE),
(7, 3, 'Elastic Shorts', 'ALL', 'Pulling up elastic waistband shorts.', 'ic_shorts', TRUE),
(8, 5, 'Socks', 'ALL', 'Putting on left and right socks.', 'ic_socks', TRUE),
(9, 5, 'Velcro Shoes', 'ALL', 'Putting on velcro shoes and securing straps.', 'ic_shoes', TRUE),
(10, 5, 'Shoes with Shoelaces', 'ALL', 'Tying shoe laces step by step.', 'ic_shoelaces', TRUE),
(11, 6, 'Eating Rice with Gravy/Curry', 'ALL', 'Mixing and eating rice with hands.', 'ic_eating_hands', TRUE),
(12, 6, 'Eating Breakfast Items with Hands', 'ALL', 'Breaking and eating idli/dosa/roti.', 'ic_breakfast', TRUE),
(13, 6, 'Eating with Spoon', 'ALL', 'Scooping and transporting food with spoon.', 'ic_spoon', TRUE);

-- Seed Task Steps for T-Shirt (Activity ID 1) - All 18 Steps
INSERT INTO task_steps (id, activity_id, step_number, title, instruction_text, child_instruction) VALUES
(1, 1, 1, 'Look at the T-shirt', 'Ask the child to focus their attention on the T-shirt placed in front of them.', 'Look at your cool T-Shirt!'),
(2, 1, 2, 'Pick up the T-shirt', 'Child picks up the T-shirt using both hands.', 'Pick up your T-shirt with both hands!'),
(3, 1, 3, 'Identify front and back', 'Child identifies tag on back or graphic on front of T-shirt.', 'Find the tag on the back!'),
(4, 1, 4, 'Find neck opening', 'Child locates the main neck collar opening.', 'Look for the big neck hole!'),
(5, 1, 5, 'Hold T-shirt at shoulder areas', 'Child grips the T-shirt near the left and right shoulder seams.', 'Hold your shirt at the shoulders!'),
(6, 1, 6, 'Lift T-shirt to chest level', 'Child raises T-shirt up toward chest height.', 'Lift the shirt up high to your chest!'),
(7, 1, 7, 'Put head through neck opening', 'Child pushes head up through the neck opening.', 'Pop your head through the neck hole! Peekaboo!'),
(8, 1, 8, 'Pull T-shirt down', 'Child pulls the collar down past the head onto the neck.', 'Pull the shirt down past your head!'),
(9, 1, 9, 'Find right sleeve', 'Child locates the right arm opening.', 'Find the right arm sleeve!'),
(10, 1, 10, 'Put right arm through sleeve', 'Child pushes right hand and arm into the right sleeve.', 'Push your right arm through!'),
(11, 1, 11, 'Pull right sleeve toward shoulder', 'Child pulls sleeve upward past elbow to shoulder.', 'Pull the right sleeve up!'),
(12, 1, 12, 'Find left sleeve', 'Child locates the left arm opening.', 'Find the left arm sleeve!'),
(13, 1, 13, 'Put left arm through sleeve', 'Child pushes left hand and arm into the left sleeve.', 'Push your left arm through!'),
(14, 1, 14, 'Pull left sleeve toward shoulder', 'Child pulls sleeve upward past elbow to shoulder.', 'Pull the left sleeve up!'),
(15, 1, 15, 'Pull front down', 'Child grabs lower hem in front and pulls down over waist.', 'Pull the front down to your tummy!'),
(16, 1, 16, 'Pull back down', 'Child reaches back hem and pulls down over lower back.', 'Pull the back down over your waist!'),
(17, 1, 17, 'Straighten T-shirt', 'Child adjusts folds and aligns T-shirt comfortably.', 'Smooth out your T-shirt!'),
(18, 1, 18, 'Check comfortable positioning', 'Child inspects neck, sleeves, and hem for comfort.', 'You look awesome! All set!');

-- Seed Micro-skills
INSERT INTO micro_skills (id, step_id, name, sequence_order, description) VALUES
(1, NULL, 'BUTTONING - Find button', 1, 'Locate button on garment'),
(2, NULL, 'BUTTONING - Find buttonhole', 2, 'Locate buttonhole opening'),
(3, NULL, 'BUTTONING - Hold button', 3, 'Pinch button between thumb and index finger'),
(4, NULL, 'BUTTONING - Hold buttonhole', 4, 'Hold fabric around buttonhole'),
(5, NULL, 'BUTTONING - Push button', 5, 'Push button half-way through hole'),
(6, NULL, 'BUTTONING - Pull button through', 6, 'Grasp button from opposite side and pull'),
(7, NULL, 'BUTTONING - Check', 7, 'Verify button is secured'),

(8, NULL, 'ZIPPER - Find slider', 1, 'Locate zipper slider at bottom'),
(9, NULL, 'ZIPPER - Find insertion pin', 2, 'Locate pin on opposite edge'),
(10, NULL, 'ZIPPER - Hold slider', 3, 'Hold slider firmly at base'),
(11, NULL, 'ZIPPER - Hold pin', 4, 'Hold insertion pin securely'),
(12, NULL, 'ZIPPER - Insert pin', 5, 'Push pin completely into slider socket'),
(13, NULL, 'ZIPPER - Stabilize garment', 6, 'Hold bottom fabric firm'),
(14, NULL, 'ZIPPER - Pull slider upward', 7, 'Pull zipper tab straight up'),
(15, NULL, 'ZIPPER - Continue', 8, 'Zip to desired height'),
(16, NULL, 'ZIPPER - Release', 9, 'Let go of zipper tab'),
(17, NULL, 'ZIPPER - Check', 10, 'Verify smooth closure'),

(18, NULL, 'DRAWSTRING - Find strings', 1, 'Locate left and right drawstring ends'),
(19, NULL, 'DRAWSTRING - Hold both ends', 2, 'Hold left end in left hand, right end in right hand'),
(20, NULL, 'DRAWSTRING - Pull', 3, 'Pull outward to tighten waist'),
(21, NULL, 'DRAWSTRING - Cross', 4, 'Cross left string over right string'),
(22, NULL, 'DRAWSTRING - Make loop', 5, 'Form initial crossover loop'),
(23, NULL, 'DRAWSTRING - Wrap', 6, 'Wrap one string under'),
(24, NULL, 'DRAWSTRING - Push through', 7, 'Tuck string through loop'),
(25, NULL, 'DRAWSTRING - Pull to secure', 8, 'Pull both ends tight'),
(26, NULL, 'DRAWSTRING - Check', 9, 'Verify comfortable tension'),

(27, NULL, 'SHOELACES - Find laces', 1, 'Grasp laces on shoe'),
(28, NULL, 'SHOELACES - Hold one in each hand', 2, 'Hold left lace in left hand, right in right hand'),
(29, NULL, 'SHOELACES - Cross', 3, 'Cross laces forming an X'),
(30, NULL, 'SHOELACES - First knot', 4, 'Pass upper lace under and pull tight'),
(31, NULL, 'SHOELACES - Make loop', 5, 'Create first bow loop (bunny ear)'),
(32, NULL, 'SHOELACES - Wrap', 6, 'Wrap free lace around loop'),
(33, NULL, 'SHOELACES - Push through', 7, 'Push middle of free lace through hole'),
(34, NULL, 'SHOELACES - Second loop', 8, 'Grasp second loop'),
(35, NULL, 'SHOELACES - Pull tight', 9, 'Pull both loops in opposite directions'),
(36, NULL, 'SHOELACES - Check', 10, 'Check knot stability');

-- Seed Rewards
INSERT INTO rewards (id, title, icon_url, stars_required, description) VALUES
(1, 'Super Dresser Star', 'ic_star_gold', 5, 'Awarded for completing dressing steps!'),
(2, 'Independence Master', 'ic_badge_trophy', 10, 'Awarded for independent completion of ADL task!'),
(3, 'Eating Champion', 'ic_star_silver', 5, 'Awarded for great mealtime independence!');
