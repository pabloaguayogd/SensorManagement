
CREATE DATABASE IF NOT EXISTS `sensor_management`;
USE `sensor_management`;

-- Volcando estructura para tabla sensor_management.grupo
CREATE TABLE IF NOT EXISTS `grupo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `mqtt_channel` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `mqtt_channel` (`mqtt_channel`)
);

-- Volcando datos para la tabla sensor_management.grupo: ~1 rows (aproximadamente)
INSERT INTO `grupo` (`id`, `name`, `mqtt_channel`) VALUES
	(1, 'Sala', '/hogar/sala');


-- Volcando estructura para tabla sensor_management.device
CREATE TABLE IF NOT EXISTS `device` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `group_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `group_id` (`group_id`),
  CONSTRAINT `device_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `grupo` (`id`)
);

-- Volcando datos para la tabla sensor_management.device: ~1 rows (aproximadamente)
INSERT INTO `device` (`id`, `name`, `group_id`) VALUES
	(1, 'ESP32_Sala', 1);


-- Volcando estructura para tabla sensor_management.actuator
CREATE TABLE IF NOT EXISTS `actuator` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `type` varchar(50) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `device_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `identifier` (`identifier`),
  KEY `device_id` (`device_id`),
  CONSTRAINT `actuator_ibfk_1` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`)
);

-- Volcando datos para la tabla sensor_management.actuator: ~1 rows (aproximadamente)
INSERT INTO `actuator` (`id`, `name`, `type`, `identifier`, `device_id`) VALUES
	(1, 'Sirena_Sala', 'relay', 'RELAY123', 1);

-- Volcando estructura para tabla sensor_management.actuatorstate
CREATE TABLE IF NOT EXISTS `actuator_state` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `actuator_id` int(11) NOT NULL,
  `state` tinyint(1) NOT NULL,
  `timestamp` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `actuator_id` (`actuator_id`),
  CONSTRAINT `actuator_state_ibfk_1` FOREIGN KEY (`actuator_id`) REFERENCES `actuator` (`id`)
);

-- Volcando datos para la tabla sensor_management.actuatorstate: ~0 rows (aproximadamente)





-- Volcando estructura para tabla sensor_management.sensor
CREATE TABLE IF NOT EXISTS `sensor` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `type` varchar(50) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `device_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `identifier` (`identifier`),
  KEY `device_id` (`device_id`),
  CONSTRAINT `sensor_ibfk_1` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`)
);

-- Volcando datos para la tabla sensor_management.sensor: ~1 rows (aproximadamente)
INSERT INTO `sensor` (`id`, `name`, `type`, `identifier`, `device_id`) VALUES
	(1, 'Movimiento_Sala', 'motion', 'MOV123', 1);

-- Volcando estructura para tabla sensor_management.sensorvalue
CREATE TABLE IF NOT EXISTS `sensorvalue` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sensor_id` int(11) NOT NULL,
  `value` float NOT NULL,
  `timestamp` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `sensor_id` (`sensor_id`),
  CONSTRAINT `sensorvalue_ibfk_1` FOREIGN KEY (`sensor_id`) REFERENCES `sensor` (`id`)
);
