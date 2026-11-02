-- Migration V12: Seed ADL Activities and Task Steps for Shoes, Eating, and Grooming Categories

-- 1. Ensure Grooming Category Exists
INSERT IGNORE INTO activity_categories (id, name, sub_category, description) VALUES
(7, 'GROOMING', 'COMMON', 'Personal hygiene, handwashing and toothbrushing ADL skills');

-- 2. Ensure Grooming Activities Exist
INSERT IGNORE INTO activities (id, category_id, title, target_gender, description, icon_url, is_active) VALUES
(14, 7, 'Toothbrushing', 'ALL', 'Brushing teeth step by step independently.', 'ic_brush', TRUE),
(15, 7, 'Handwashing', 'ALL', 'Washing hands with soap and water step by step.', 'ic_handwashing', TRUE);

-- 3. Seed Task Steps for Velcro Shoes (Activity ID 9)
INSERT IGNORE INTO task_steps (id, activity_id, step_number, title, instruction_text, child_instruction) VALUES
(101, 9, 1, 'Unfasten shoe straps', 'Child unpeels velcro straps on the shoe.', 'Pull the velcro straps open!'),
(102, 9, 2, 'Open shoe wide', 'Child pulls tongue and collar wide open.', 'Pull the tongue open wide!'),
(103, 9, 3, 'Align sock with foot', 'Child inspects sock heel positioning before donning.', 'Make sure your sock is smooth!'),
(104, 9, 4, 'Pull sock over toes', 'Child slides toe portion of sock over toes.', 'Slide the sock over your toes!'),
(105, 9, 5, 'Pull sock over heel', 'Child pulls heel cup of sock securely past heel.', 'Pull the sock snug over your heel!'),
(106, 9, 6, 'Insert toes into shoe', 'Child guides foot forward into the shoe opening.', 'Push your toes right into the shoe!'),
(107, 9, 7, 'Push heel down into shoe', 'Child firmly pushes heel down into shoe bed.', 'Push your heel all the way down!'),
(108, 9, 8, 'Pull tongue straight', 'Child straightens tongue flat against instep.', 'Pull the tongue flat and straight!'),
(109, 9, 9, 'Fasten shoe strap', 'Child pulls velcro strap across and presses down firmly.', 'Press the velcro strap down tight!'),
(110, 9, 10, 'Check shoe fit', 'Child stands and tests comfortable foot positioning.', 'Stomp your foot! You are ready to go!');

-- 4. Seed Task Steps for Eating with Spoon (Activity ID 13)
INSERT IGNORE INTO task_steps (id, activity_id, step_number, title, instruction_text, child_instruction) VALUES
(121, 13, 1, 'Grasp spoon handle', 'Child grips spoon using radial-digital or palmar grasp.', 'Hold your spoon firmly in your hand!'),
(122, 13, 2, 'Dip spoon into bowl', 'Child lowers bowl of spoon into food dish.', 'Lower the spoon into your bowl!'),
(123, 13, 3, 'Scoop food onto spoon', 'Child scoops an appropriate portion onto the spoon.', 'Scoop a yummy bite onto your spoon!'),
(124, 13, 4, 'Lift spoon carefully', 'Child raises spoon without tilting or spilling.', 'Lift your spoon up carefully!'),
(125, 13, 5, 'Bring spoon to mouth', 'Child moves spoon in horizontal trajectory toward mouth.', 'Bring the spoon up to your mouth!'),
(126, 13, 6, 'Open mouth', 'Child opens mouth in anticipation of spoon insertion.', 'Open wide! Say ahh!'),
(127, 13, 7, 'Place spoon inside mouth', 'Child places food portion over tongue.', 'Put the spoon gently in your mouth!'),
(128, 13, 8, 'Close lips around spoon', 'Child uses upper lip to clear food from spoon bowl.', 'Close your lips over the food!'),
(129, 13, 9, 'Remove empty spoon', 'Child withdraws spoon horizontally without scraping teeth.', 'Take the spoon out!'),
(130, 13, 10, 'Chew and swallow food', 'Child masticates and swallows food safely.', 'Chew nicely and swallow! Great job!');

-- 5. Seed Task Steps for Toothbrushing (Activity ID 14)
INSERT IGNORE INTO task_steps (id, activity_id, step_number, title, instruction_text, child_instruction) VALUES
(141, 14, 1, 'Pick up toothbrush', 'Child grasps toothbrush handle securely.', 'Pick up your toothbrush by the handle!'),
(142, 14, 2, 'Apply toothpaste', 'Child squeezes a pea-sized amount of toothpaste onto bristles.', 'Put a tiny pea of toothpaste on top!'),
(143, 14, 3, 'Brush outside of teeth', 'Child brushes front and lateral dental surfaces gently.', 'Brush the outside of your teeth round and round!'),
(144, 14, 4, 'Brush chewing surfaces', 'Child brushes horizontal biting surfaces of molars.', 'Brush the tops of your chewing teeth!'),
(145, 14, 5, 'Brush inside surfaces', 'Child brushes lingual and palatal dental surfaces.', 'Brush the inside of your teeth gently!'),
(146, 14, 6, 'Rinse mouth with water', 'Child takes sip of water, swishes, and expels into sink.', 'Swish water in your mouth and spit!'),
(147, 14, 7, 'Rinse toothbrush and put away', 'Child rinses brush bristles and returns brush to holder.', 'Rinse your brush under the tap and smile!');

