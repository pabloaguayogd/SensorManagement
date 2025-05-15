package com.sensor.app.entities;

import io.vertx.sqlclient.Row;

import java.time.LocalDateTime;
import java.util.Objects;

public class SensorValue {

    public static final String CREATE_SENSOR_VALUE = "INSERT INTO Sensor_value (sensor_id, value, timestamp) VALUES (?, ?, ?)";
    public static final String GET_SENSOR_VALUE_ID = "SELECT * FROM Sensor_value WHERE sensor_id = ?";

    private int sensor_value_id;
    private int sensorId;
    private float value;
    private LocalDateTime timestamp;

    public SensorValue() {}

    public SensorValue(Row row) {

        LocalDateTime timestamp = row.get(LocalDateTime.class,"timestamp");

        setSensor_value_id(row.getInteger("id"));
        setSensorId(row.getInteger("sensor_id"));
        setValue(row.getFloat("value"));
        setTimestamp(timestamp);

    }

    public SensorValue(int sensor_value_id, int sensorId, float value, LocalDateTime timestamp) {
        this.sensor_value_id = sensor_value_id;
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
    }

    public int getSensor_value_id() {
        return sensor_value_id;
    }

    public void setSensor_value_id(int sensor_value_id) {
        this.sensor_value_id = sensor_value_id;
    }

    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensor_value_id, sensorId, value, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SensorValue other = (SensorValue) obj;
        return sensor_value_id == other.sensor_value_id &&
               sensorId == other.sensorId &&
               Float.compare(value, other.value) == 0 &&
               Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return "SensorValue [id=" + sensor_value_id + ", sensorId=" + sensorId +
               ", value=" + value + ", timestamp=" + timestamp + "]";
    }
}
