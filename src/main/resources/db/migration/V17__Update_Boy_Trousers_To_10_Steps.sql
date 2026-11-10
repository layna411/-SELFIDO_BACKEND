-- Flyway Migration V17: Update Activity #5 (Boy Trousers) to 10 Clinical Steps matching Trouser GIFs 1-10

-- 1. Update Activity Metadata
UPDATE activities SET
    title = 'Trousers',
    title_ta = 'கால்சட்டை',
    description = '10 Clinical task-analysis steps for wearing trousers independently.',
    description_ta = 'கால்சட்டை அணியும் 10 படிநிலை பயிற்சிகள்.',
    starting_position = 'Trousers are placed flat in front of the child with the waistband open and the front side facing upward.',
    starting_position_ta = 'கால்சட்டை இடுப்புப் பகுதி திறக்கப்பட்டு முன்புறம் மேல்நோக்கி வைக்கப்பட்டுள்ளது.',
    is_active = TRUE
WHERE id = 5;

-- 2. Safely remove FK references to steps > 10 for activity 5
DELETE FROM caregiver_practice_step_results WHERE step_id IN (SELECT id FROM task_steps WHERE activity_id = 5 AND step_number > 10);
DELETE FROM prompt_fading_histories WHERE step_id IN (SELECT id FROM task_steps WHERE activity_id = 5 AND step_number > 10);
DELETE FROM prompt_fading_plans WHERE step_id IN (SELECT id FROM task_steps WHERE activity_id = 5 AND step_number > 10);
DELETE FROM home_program_steps WHERE step_id IN (SELECT id FROM task_steps WHERE activity_id = 5 AND step_number > 10);
DELETE FROM performance_records WHERE step_id IN (SELECT id FROM task_steps WHERE activity_id = 5 AND step_number > 10);
DELETE FROM assessment_results WHERE step_id IN (SELECT id FROM task_steps WHERE activity_id = 5 AND step_number > 10);
DELETE FROM step_media WHERE step_id IN (SELECT id FROM task_steps WHERE activity_id = 5);

-- 3. Remove steps > 10 for activity 5
DELETE FROM task_steps WHERE activity_id = 5 AND step_number > 10;

-- 4. Upsert exact 10 clinical bilingual steps matching GIFs 1..10
INSERT INTO task_steps (id, activity_id, step_number, title, title_ta, instruction_text, instruction_text_ta, child_instruction, child_instruction_ta) VALUES
(501, 5, 1, 'Stand in front of the trousers', 'கால்சட்டையின் முன் நிற்கவும்', 'Ask the child to stand calmly facing the open trousers placed flat on the floor.', 'தரையில் விரிக்கப்பட்டுள்ள கால்சட்டைக்கு முன் குழந்தையை நேராக நிற்க வைக்கவும்.', 'Stand in front of your trousers!', 'கால்சட்டைக்கு முன் நில்!'),
(502, 5, 2, 'Look at the trousers', 'கால்சட்டையைப் பார்க்கவும்', 'Encourage the child to visually focus and point at the waistband and leg openings.', 'கால்சட்டையின் இடுப்புப் பட்டை மற்றும் கால் பகுதிகளைக் கூர்ந்து பார்க்கச் சொல்லவும்.', 'Look at your trousers!', 'கால்சட்டையைப் பார்!'),
(503, 5, 3, 'Pick up the trousers using both hands', 'இரு கைகளாலும் கால்சட்டையை எடுக்கவும்', 'Guide the child to bend down, hold the waistband with both hands, and lift the trousers up.', 'குனிந்து இரு கைகளாலும் கால்சட்டையின் மேல் விளிம்பைப் பிடித்துத் தூக்க வழிகாட்டவும்.', 'Pick up your trousers with both hands!', 'இரு கைகளாலும் கால்சட்டையை எடு!'),
(504, 5, 4, 'Identify the front and back', 'முன்புறம் மற்றும் பின்புறத்தை அறியவும்', 'Help the child locate the white drawstring on the front and seams on the back.', 'முன்புற நாடா மற்றும் பின்பக்கப் பைகளைப் பார்த்து முன்பின் பக்கங்களை அடையாளம் காண உதவவும்.', 'Check the front drawstring and the back!', 'முன்பக்க நாடாவையும் பின்பக்கத்தையும் பார்!'),
(505, 5, 5, 'Find the waistband', 'இடுப்புப் பட்டையைக் கண்டறியவும்', 'Guide the child to trace and hold the circular elastic waistband at the top.', 'மேல் பகுதியில் உள்ள இடுப்புப் பட்டையைத் தொட்டுக் கண்டறியச் சொல்லவும்.', 'Find the waistband at the top!', 'இடுப்புப் பட்டையைத் தொடு!'),
(506, 5, 6, 'Find the right leg opening', 'வலது கால் துவாரத்தைக் கண்டறியவும்', 'Prompt the child to point out the right leg opening at the bottom of the trousers.', 'வலது கால் நுழையும் கீழ் துவாரத்தைச் சுட்டிக்காட்ட உதவவும்.', 'Find the right leg hole!', 'வலது கால் துவாரத்தைக் கண்டுபிடி!'),
(507, 5, 7, 'Find the left leg opening', 'இடது கால் துவாரத்தைக் கண்டறியவும்', 'Prompt the child to identify and touch the left leg opening of the trousers.', 'இடது கால் நுழையும் துவாரத்தைத் தொட்டு அடையாளம் காணச் சொல்லவும்.', 'Find the left leg hole!', 'இடது கால் துவாரத்தைக் கண்டுபிடி!'),
(508, 5, 8, 'Hold the waistband open with both hands', 'இடுப்புப் பட்டையை இரு கைகளாலும் விரித்துப் பிடிக்கவும்', 'Ask the child to hold the left and right edges of the waistband open cleanly for stepping in.', 'கால் நுழைவதற்கு ஏதுவாக இடுப்புப் பட்டையை இரு கைகளாலும் அகலமாக விரித்துப் பிடிக்கச் சொல்லவும்.', 'Hold the waistband open wide!', 'இடுப்புப் பட்டையை அகலமாகப் பிடி!'),
(509, 5, 9, 'Step your right leg into the right leg opening', 'வலது காலை வலது துவாரத்தில் நுழைக்கவும்', 'Support the child''s balance as they lift their right foot into the right opening and extend it through.', 'சமநிலையுடன் வலது காலைத் தூக்கி வலது கால் துவாரத்தின் வழியே கீழே கொண்டு வர உதவவும்.', 'Step your right foot into the right leg hole!', 'வலது காலை வலது துளையில் வை!'),
(510, 5, 10, 'Step your left leg in and pull the trousers up', 'இடது காலை நுழைத்து கால்சட்டையை மேலே இழுக்கவும்', 'Guide the child to insert the left foot through the left opening, stand steadily, and pull the waistband evenly up to the waist.', 'இடது காலை மற்றொரு துவாரத்தில் நுழைத்து, நேராக நின்று இடுப்புப் பட்டையை மேலே இழுத்து சரிசெய்ய வழிகாட்டவும்.', 'Step your left foot in and pull your trousers up! Awesome!', 'இடது காலையும் உள்ளே வைத்து கால்சட்டையை மேலே இழு! சூப்பர்!')
ON DUPLICATE KEY UPDATE
    step_number = VALUES(step_number),
    title = VALUES(title),
    title_ta = VALUES(title_ta),
    instruction_text = VALUES(instruction_text),
    instruction_text_ta = VALUES(instruction_text_ta),
    child_instruction = VALUES(child_instruction),
    child_instruction_ta = VALUES(child_instruction_ta);

-- 5. Seed direct GIF media paths for Steps 1..10
INSERT INTO step_media (step_id, media_type, media_url, thumbnail_url, caption) VALUES
(501, 'IMAGE_GIF', 'file:///android_asset/dressing/boy/trousers/step_01.gif', 'file:///android_asset/dressing/boy/trousers/step_01.gif', 'Stand in front of the trousers'),
(502, 'IMAGE_GIF', 'file:///android_asset/dressing/boy/trousers/step_02.gif', 'file:///android_asset/dressing/boy/trousers/step_02.gif', 'Look at the trousers'),
(503, 'IMAGE_GIF', 'file:///android_asset/dressing/boy/trousers/step_03.gif', 'file:///android_asset/dressing/boy/trousers/step_03.gif', 'Pick up the trousers using both hands'),
(504, 'IMAGE_GIF', 'file:///android_asset/dressing/boy/trousers/step_04.gif', 'file:///android_asset/dressing/boy/trousers/step_04.gif', 'Identify the front and back'),
(505, 'IMAGE_GIF', 'file:///android_asset/dressing/boy/trousers/step_05.gif', 'file:///android_asset/dressing/boy/trousers/step_05.gif', 'Find the waistband'),
(506, 'IMAGE_GIF', 'file:///android_asset/dressing/boy/trousers/step_06.gif', 'file:///android_asset/dressing/boy/trousers/step_06.gif', 'Find the right leg opening'),
(507, 'IMAGE_GIF', 'file:///android_asset/dressing/boy/trousers/step_07.gif', 'file:///android_asset/dressing/boy/trousers/step_07.gif', 'Find the left leg opening'),
(508, 'IMAGE_GIF', 'file:///android_asset/dressing/boy/trousers/step_08.gif', 'file:///android_asset/dressing/boy/trousers/step_08.gif', 'Hold the waistband open with both hands'),
(509, 'IMAGE_GIF', 'file:///android_asset/dressing/boy/trousers/step_09.gif', 'file:///android_asset/dressing/boy/trousers/step_09.gif', 'Step your right leg into the right leg opening'),
(510, 'IMAGE_GIF', 'file:///android_asset/dressing/boy/trousers/step_10.gif', 'file:///android_asset/dressing/boy/trousers/step_10.gif', 'Step your left leg in and pull the trousers up');

