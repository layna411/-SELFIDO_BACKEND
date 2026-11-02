-- Migration V13: Seed Task Steps for Handwashing (Activity ID 15)

INSERT IGNORE INTO task_steps (id, activity_id, step_number, title, instruction_text, child_instruction) VALUES
(151, 15, 1, 'Turn on water tap', 'Child turns on the water tap to initiate a gentle flow of water.', 'Turn on the tap so the water runs!'),
(152, 15, 2, 'Wet hands under water', 'Child places both hands under running water until thoroughly wet.', 'Put your hands under the water to get them wet!'),
(153, 15, 3, 'Apply soap', 'Child depresses soap pump or rubs soap bar to apply soap to palms.', 'Push the pump to get bubbly soap!'),
(154, 15, 4, 'Rub palms and fingers', 'Child rubs palms together, between fingers, and over backs of hands for 20 seconds.', 'Rub your hands together to make lots of soapy bubbles!'),
(155, 15, 5, 'Rinse soap off', 'Child holds hands under clean running water until all soap bubbles are rinsed away.', 'Rinse all the bubbles away under the tap!'),
(156, 15, 6, 'Turn off water tap', 'Child turns off the water tap securely to stop water flow.', 'Turn off the tap nice and tight!'),
(157, 15, 7, 'Dry hands with towel', 'Child uses clean hand towel to pat and wipe both hands dry.', 'Dry your hands with the towel! Super clean!');

