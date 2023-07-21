INSERT INTO `agv_data`(`name`, `memo`) VALUES('AGV#1', '400公斤拖板型');

INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(9999, 'Station1-1', '第ㄧ大站第一格車位');
INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(9999, 'Station1-2', '第ㄧ大站第二格車位');
INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(9999, 'Station1-3', '第ㄧ大站第三格車位');
INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(9999, 'Station1-4', '第ㄧ大站第四格車位');
INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(9999, 'Station1-5', '第ㄧ大站第五格車位');

INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(1001, 'Station2-1', '第二大站第一格車位');
INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(1011, 'Station2-2', '第二大站第二格車位');
INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(1021, 'Station2-3', '第二大站第三格車位');
INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(1031, 'Station2-4', '第二大站第四格車位');
INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(1041, 'Station2-5', '第二大站第五格車位');

INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(1051, 'Station3-1', '第三大站第一格車位');
INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(1061, 'Station3-2', '第三大站第二格車位');
INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(1071, 'Station3-3', '第三大站第三格車位');
INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(1081, 'Station3-4', '第三大站第四格車位');
INSERT INTO `station_data`(`tag`, `name`, `memo`) VALUES(1091, 'Station3-5', '第三大站第五格車位');

INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('PCB測試', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('PCB外線', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('PCB外AOI', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('PCB網印', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('CNC二廠', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('FQC', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('BGA整面C', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('棕化', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('內層線路', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('Suep', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('FVI', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('PCB噴塗', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('BGA整面A', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('CNC一廠', '');
INSERT INTO `notification_station_data`(`name`, `memo`) VALUES('Routing', '');

INSERT INTO `mode_data`(`mode`, `name`, `memo`) VALUES(1, 'transport', '物件轉載。');

INSERT INTO `message_data`(`level`, `content`) VALUES(1, 'AGV online');
INSERT INTO `message_data`(`level`, `content`) VALUES(1, 'AGV offline');
INSERT INTO `message_data`(`level`, `content`) VALUES(2, 'AGV collided');

INSERT INTO `notification_title_data`(`name`, `memo`) VALUES('AGV System', 'AGV System');
INSERT INTO `notification_title_data`(`name`, `memo`) VALUES('AGV#1', 'AGV#1');
INSERT INTO `notification_title_data`(`name`, `memo`) VALUES('AGV#2', 'AGV#2');
INSERT INTO `notification_title_data`(`name`, `memo`) VALUES('Station1 Beeper', 'Station1 Beeper');

INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306140001', '20230614110422', 1, 6, 3, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306140002', '20230614110512', 2, 2, 3, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306140003', '20230614110632', 3, 3, 4, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306140004', '20230614120112', 1, 4, 2, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id` , `mode_id`)
					VALUES(1, '#202306140005', '20230614121451', 1, 2, 3, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306140006', '20230614121839', 3, 1, 3, 1);

INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306150001', '20230615110422', 1, 1, 3, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306150002', '20230615110512', 2, 2, 3, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306150003', '20230615110632', 3, 3, 4, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306150004', '20230615120112', 1, 4, 2, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306150005', '20230615121451', 1, 2, 3, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306150006', '20230615121839', 3, 1, 3, 1);

INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306180001', '20230618110422', 1, 1, 3, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306180002', '20230618110512', 2, 2, 3, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306180003', '20230618110632', 3, 3, 4, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306180004', '20230618120112', 1, 4, 2, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306180005', '20230618121451', 1, 2, 3, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306180006', '20230618121839', 3, 1, 3, 1);

INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306210001', '20230621110422', 1, 1, 3, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306210002', '20230621110512', 2, 2, 3, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306210003', '20230621110632', 3, 3, 4, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306210004', '20230621120112', 1, 4, 2, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306210005', '20230621121451', 1, 2, 3, 1);
INSERT INTO `task_history`(`agv_id`, `task_number`, `create_task_time`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)
					VALUES(1, '#202306210006', '20230621121839', 3, 1, 3, 1);


INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 1, '20230615110422');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 3, '20230615110512');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 3, '20230615110632');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 3, '20230615120112');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 1, '20230615121451');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 3, '20230615121839');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 2, '20230620110422');

INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 1, '20230621110422');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 3, '20230621110512');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 3, '20230621110632');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 3, '20230621120112');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 2, '20230621121451');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 1, '20230621121839');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 2, '20230626110422');

INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 1, '20230627110422');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 2, '20230627110512');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 1, '20230627110632');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 3, '20230627120112');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 2, '20230627121451');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 1, '20230627121839');
INSERT INTO `notification_history`(`title_id`, `message_id`, `create_time`) VALUES(2, 2, '20230628110422');

INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,1,2,600,840,42);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,2,3,600,840,40);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,3,4,480,840,33);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,4,5,360,840,22);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,5,6,540,840,34);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,6,7,720,840,41);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,7,1,720,840,49);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,8,2,360,840,28);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,9,3,480,840,35);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,10,4,540,840,34);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,11,5,660,840,42);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,12,6,720,840,44);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,13,7,600,840,38);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,14,1,360,840,22);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,15,2,420,840,29);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,16,3,480,840,32);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,17,4,540,840,34);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,18,5,600,840,38);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,19,6,360,840,23);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,20,7,660,840,40);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,21,1,540,840,38);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,22,2,780,840,55);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,23,3,720,840,45);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,24,4,600,840,32);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,25,5,540,840,29);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,26,6,600,840,34);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,27,7,660,840,38);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,28,1,660,840,42);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,29,2,720,840,48);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,11,30,3,540,840,33);

INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,1,4,600,840,42);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,2,5,600,840,40);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,3,6,480,840,33);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,4,7,360,840,22);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,5,1,540,840,34);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,6,2,720,840,41);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,7,3,720,840,49);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,8,4,360,840,28);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,9,5,480,840,35);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,10,6,540,840,34);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,11,7,660,840,42);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,12,1,720,840,44);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,13,2,600,840,38);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,14,3,360,840,22);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,15,4,420,840,29);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,16,5,480,840,32);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,17,6,429,840,34);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,18,7,432,840,38);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,19,1,733,840,23);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,20,2,344,840,40);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,21,3,544,840,38);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,22,4,433,840,55);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,23,5,733,840,45);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,24,6,293,840,32);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,25,7,530,840,29);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,26,1,649,840,34);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,27,2,740,840,38);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,28,3,623,840,42);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,29,4,720,840,48);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,30,5,532,840,33);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2022,12,31,6,666,840,40);

INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,1,7,603,840,42);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,2,1,594,840,40);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,3,2,444,840,33);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,4,3,420,840,22);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,5,4,500,840,34);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,6,5,740,840,41);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,7,6,777,840,49);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,8,7,330,840,28);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,9,1,422,840,35);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,10,2,555,840,34);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,11,3,666,840,42);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,12,4,664,840,44);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,13,5,654,840,38);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,14,6,410,840,22);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,15,7,405,840,29);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,16,1,444,840,32);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,17,2,749,840,34);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,18,3,223,840,38);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,19,4,283,840,23);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,20,5,653,840,40);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,21,6,661,840,38);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,22,7,770,840,55);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,23,1,810,840,45);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,24,2,533,840,32);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,25,3,532,840,29);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,26,4,344,840,34);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,27,5,443,840,38);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,28,6,662,840,42);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,29,7,728,840,48);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,30,1,629,840,33);
INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(1,2023,1,31,2,331,14,10);
