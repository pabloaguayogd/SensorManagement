
CREATE DATABASE IF NOT EXISTS sensor_management;
USE sensor_management;

CREATE TABLE IF NOT EXISTS `User` (
    user_id INT(11) NOT NULL AUTO_INCREMENT,
    nickname VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id)
);

ALTER TABLE `User` ADD UNIQUE KEY `nickname_password`(nickname, password);

CREATE TABLE IF NOT EXISTS Notification (
    notification_id INT(11) NOT NULL AUTO_INCREMENT,
    `timestamp` datetime DEFAULT current_timestamp(),
    message TEXT NOT NULL,
    has_read tinyint(1) NOT NULL,
    user_id INT(11) DEFAULT NULL,
    PRIMARY KEY (notification_id),
    CONSTRAINT `notification_user_ibfk` FOREIGN KEY (user_id) REFERENCES `User` (user_id)
);

CREATE TABLE IF NOT EXISTS Home (
    home_id INT(11) NOT NULL AUTO_INCREMENT,
    user_id INT(11) DEFAULT NULL,
    PRIMARY KEY (home_id),
    CONSTRAINT `home_user_ibfk` FOREIGN KEY (user_id) REFERENCES `User` (user_id)
);

CREATE TABLE IF NOT EXISTS `Group` (
  group_id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(100) NOT NULL,
  mqtt_channel varchar(255) NOT NULL,
  home_id int(11) NOT NULL,
  suppressed tinyint(1) DEFAULT NULL,
  PRIMARY KEY (group_id),
  UNIQUE KEY mqtt_channel (mqtt_channel),
  CONSTRAINT `group_home_ibfk` FOREIGN KEY (home_id) REFERENCES Home (home_id),
  CONSTRAINT UNIQUE KEY (name, home_id)
);



CREATE TABLE IF NOT EXISTS Device (
  device_id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(100) NOT NULL,
  group_id int(11) DEFAULT NULL,
  PRIMARY KEY (device_id),
  KEY group_id (group_id),
  CONSTRAINT `device_ibfk_1` FOREIGN KEY (group_id) REFERENCES `Group` (group_id),
  KEY (name, group_id)
);

CREATE TABLE IF NOT EXISTS Alarm(
  alarm_id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(100) NOT NULL,
  type varchar(50) NOT NULL,
  device_id int(11) NOT NULL,
  PRIMARY KEY (alarm_id),
  KEY device_id (device_id),
  CONSTRAINT `actuator_ibfk_1` FOREIGN KEY (device_id) REFERENCES Device (device_id)
);

CREATE TABLE IF NOT EXISTS Alarm_state (
  alarm_state_id int(11) NOT NULL AUTO_INCREMENT,
  alarm_id int(11) NOT NULL,
  state tinyint(1) NOT NULL,
  `timestamp` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (alarm_state_id),
  KEY alarm_id (alarm_id),
  CONSTRAINT `actuator_state_ibfk_1` FOREIGN KEY (alarm_id) REFERENCES Alarm (alarm_id)
);

CREATE TABLE IF NOT EXISTS Sensor (
  sensor_id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(100) NOT NULL,
  type varchar(50) NOT NULL,
  device_id int(11) NOT NULL,
  PRIMARY KEY (sensor_id),
  KEY device_id (device_id),
  CONSTRAINT `sensor_ibfk_1` FOREIGN KEY (device_id) REFERENCES Device (device_id)
);

CREATE TABLE IF NOT EXISTS Sensor_value (
  sensor_value_id int(11) NOT NULL AUTO_INCREMENT,
  sensor_id int(11) NOT NULL,
  `value` float NOT NULL,
  `timestamp` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (sensor_value_id),
  KEY sensor_id (sensor_id),
  CONSTRAINT `sensorvalue_ibfk_1` FOREIGN KEY (sensor_id) REFERENCES Sensor (sensor_id)
);