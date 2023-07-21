CREATE DATABASE `YIDAGV`; -- 建造DATABASE

CREATE TABLE `agv_data`( -- 建造agv_data TABLE
	`id` int AUTO_INCREMENT PRIMARY KEY, -- 主鍵
    `name` varchar(20) NOT NULL, -- AGV名稱
    `memo` varchar(50) NOT NULL -- 備忘錄
);

CREATE TABLE `station_data`( -- 建造station_data TABLE 車站
	`id` int AUTO_INCREMENT PRIMARY KEY, -- 主鍵
    `tag` int NOT NULL, -- tag
    `name` varchar(20) NOT NULL, -- 車站名稱
    `memo` varchar(50) NOT NULL -- 備忘錄
);

CREATE TABLE `notification_station_data`( -- 建造notification_station_data TABLE 通知站
	`id` int AUTO_INCREMENT PRIMARY KEY, -- 主鍵
    `name` varchar(20) NOT NULL, -- 通知站名稱
    `memo` varchar(50) NOT NULL -- 備忘錄
);

CREATE TABLE `mode_data`( -- 建造mode TABLE
	`id` int AUTO_INCREMENT PRIMARY KEY, -- 主鍵
    `mode` int NOT NULL, -- mode
    `name` varchar(20) NOT NULL, -- 模式名稱
    `memo` varchar(50) NOT NULL -- 備忘錄
);

CREATE TABLE `message_data`( -- 建造message_data TABLE for notification_history
	`id` int AUTO_INCREMENT PRIMARY KEY, -- 主鍵
    `level` int NOT NULL, -- 通知等級
    `content` varchar(50) NOT NULL -- 通知訊息
);

CREATE TABLE `notification_title_data`( -- 建造notification_title_data TABLE for notification_history
	`id` int AUTO_INCREMENT PRIMARY KEY, -- 主鍵
    `name` varchar(20) NOT NULL, -- 名稱
    `memo` varchar(30) NOT NULL -- 備忘錄
);


CREATE TABLE `task_history`( -- 建造task_history TABLE
	`id` int AUTO_INCREMENT PRIMARY KEY, -- 主鍵
    `task_number` varchar(20) unique, -- 任務ID
    `create_task_time` varchar(20), -- 創建時間
    `agv_id` int NOT NULL,FOREIGN KEY (`agv_id`) REFERENCES `agv_data`(`id`)ON DELETE CASCADE, -- agvID
    `start_id` int,FOREIGN KEY (`start_id`) REFERENCES `station_data`(`id`)ON DELETE CASCADE, -- 車站ID
    `terminal_id` int,FOREIGN KEY (`terminal_id`) REFERENCES `station_data`(`id`)ON DELETE CASCADE, -- 車站ID
    `notification_id` int,FOREIGN KEY (`notification_id`) REFERENCES `notification_station_data`(`id`)ON DELETE CASCADE, -- 通知站ID
    `mode_id` int NOT NULL,FOREIGN KEY (`mode_id`) REFERENCES `mode_data`(`id`)ON DELETE CASCADE, -- 模式ID
    `status` int NOT NULL default 0 -- 是否完成
);
CREATE INDEX idx_task_number ON task_history (task_number);

CREATE TABLE `notification_history`( -- 建造notify_history TABLE
	`id` int AUTO_INCREMENT PRIMARY KEY, -- 主鍵
    `title_id` int NOT NULL,FOREIGN KEY (`title_id`) REFERENCES `notification_title_data`(`id`)ON DELETE CASCADE, -- 標題
    `message_id` int NOT NULL,FOREIGN KEY (`message_id`) REFERENCES `message_data`(`id`)ON DELETE CASCADE, -- 內容
    `create_time` varchar(20) -- 創建時間
);

CREATE TABLE `analysis`( -- 建造analysis TABLE
    `analysis_id` bigint AUTO_INCREMENT PRIMARY KEY, -- 主鍵
    `agv_id` int NOT NULL,FOREIGN KEY (`agv_id`) REFERENCES `agv_data`(`id`)ON DELETE CASCADE,
    `year` int NOT NULL, -- 年
    `month` int NOT NULL, -- 月
    `day` int NOT NULL, -- 日
    `week` int NOT NULL, -- 星期
    `working_minute` int, -- 工作時數
    `open_minute` int, -- 開機時數
    `task` int -- 任務數
);
ALTER TABLE `analysis` ADD UNIQUE (`agv_id`, `year`, `month`, `day`);