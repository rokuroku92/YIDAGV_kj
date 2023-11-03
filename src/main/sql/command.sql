SHOW DATABASES;
SHOW TABLES;
SHOW VARIABLES LIKE 'port';
SELECT DATABASE();
DROP TABLE `analysis`;
DROP TABLE `station_data`;
drop table task_history;
DROP TABLE `notification_history`;
DROP TABLE `notification_title_data`;
UPDATE `task_history` SET `status` = 100 WHERE (`task_number` = '#202306300008');
select DISTINCT agv_id,analysis_id from analysis order by analysis_id DESC LIMIT 3;
SELECT agv_id, MAX(analysis_id) FROM analysis WHERE agv_id IN (1, 2, 3) GROUP BY agv_id ORDER BY agv_id;

SELECT * FROM task_history WHERE DATE_FORMAT(STR_TO_DATE(create_task_time, '%Y%m%d%H%i%s'), '%Y-%m-%d') = CURDATE() ORDER BY id DESC;
SELECT * FROM task_history;
SELECT * FROM analysis;
SELECT * FROM analysis WHERE analysis_id = 184;
SELECT year, month, day FROM analysis WHERE (year, month, day) <= (SELECT MAX(year), MAX(month), MAX(day) FROM analysis) ORDER BY year DESC, month DESC, day DESC LIMIT 1;
SELECT agv_id, MAX(analysis_id) as analysis_id FROM analysis WHERE agv_id IN (1, 2, 3) GROUP BY agv_id ORDER BY agv_id;
SELECT * FROM station_data;
SELECT * FROM mode;
SELECT * FROM notification_history WHERE DATE_FORMAT(STR_TO_DATE(create_time, '%Y%m%d%H%i%s'), '%Y-%m-%d') = CURDATE() ORDER BY id DESC;

SELECT td.id, td.task_number, tdt.name AS title, td.sequence, sd.name AS start, sd.id AS start_id,
sdd.name AS terminal, sdd.tag AS terminal_tag, md.mode AS mode, md.memo AS mode_memo, td.status FROM task_detail td
INNER JOIN task_detail_title tdt ON td.title_id = tdt.id
LEFT JOIN station_data sd ON td.start_id = sd.id
LEFT JOIN station_data sdd ON td.terminal_id = sdd.id
INNER JOIN mode_data md ON td.mode_id = md.id WHERE task_number = "#YE202310310002" ORDER BY td.sequence ;

SELECT tl.id, tl.task_number, tl.create_task_time, tl.steps, tl.progress, tp.name AS phase, tl.status
	FROM task_list tl INNER JOIN task_phase tp ON tl.phase_id = tp.id ORDER BY id DESC LIMIT 100;

SELECT * FROM task_detail;
SELECT * FROM task_list;
SELECT * FROM task_phase;
SELECT * FROM now_task_list;
SElECT * FROM now_task_list WHERE task_number LIKE "#YE%" OR task_number LIKE "#RE%" ORDER BY id;
INSERT INTO `now_task_list`(`task_number`, `steps`) VALUES('#YE202311030001', 8);
INSERT INTO `now_task_list`(`task_number`, `steps`) VALUES('#RE202311030002', 8);
INSERT INTO `now_task_list`(`task_number`, `steps`) VALUES('#NE202311030003', 8);
INSERT INTO `now_task_list`(`task_number`, `steps`) VALUES('#YE202311030004', 8);
INSERT INTO `now_task_list`(`task_number`, `steps`) VALUES('#RE202311030005', 8);
INSERT INTO `now_task_list`(`task_number`, `steps`) VALUES('#NE202311030006', 8);

UPDATE `task_detail` SET `status` = 100 WHERE `task_number` = '#YE202310310002' AND `sequence` = 7;

SELECT ntl.id, ntl.task_number, ntl.steps, ntl.progress, tp.name AS phase
FROM now_task_list ntl INNER JOIN task_phase tp ON ntl.phase_id = tp.id ORDER BY id;

UPDATE `now_task_list` SET `phase_id` = 5 WHERE `task_number` = "#YE202310310002";

SELECT * FROM `station_data` WHERE `name` LIKE '%-S';

SELECT * FROM station_data;
SELECT * FROM grid_list;

SELECT gl.id, sd.name AS station, gl.status, gl.work_number_1, gl.work_number_2, gl.work_number_3, gl.work_number_4,
gl.object_name_1, gl.object_name_2, gl.object_name_3, gl.object_name_4, gl.object_number_1, gl.object_number_2, gl.object_number_3, gl.object_number_4,
gl.create_time FROM grid_list gl
INNER JOIN station_data sd ON gl.station_id = sd.id ORDER BY id;

UPDATE `grid_list` SET `status` = 2 WHERE `station_id` = 20;
UPDATE `grid_list` SET `work_number_1` = "WCDS004-AJ-S2", `work_number_2` = "WCDS005-AJ-S1", `work_number_3` = "WCDS005-AJ-S2"
, `object_name_1` = "apple" , `object_name_2` = "banana" , `object_name_3` = "orange" , `object_number_1` = "WCDS003"
, `object_number_2` = "WCDS004" , `object_number_3` = "WCDS006", `create_time` = "20231101132223" WHERE `station_id` = 20;
UPDATE `grid_list` SET `work_number_1` = "WCDS004-AJ-S2" WHERE `station_id` = 20;