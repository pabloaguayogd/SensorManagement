package com.sensor.app.mysql.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class SensorValue {

    private int id;
    private int sensorId;
    private float value;
    private LocalDateTime timestamp;

    public SensorValue() {}

    public SensorValue(int id, int sensorId, float value, LocalDateTime timestamp) {
        this.id = id;
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        return Objects.hash(id, sensorId, value, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SensorValue other = (SensorValue) obj;
        return id == other.id &&
               sensorId == other.sensorId &&
               Float.compare(value, other.value) == 0 &&
               Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return "SensorValue [id=" + id + ", sensorId=" + sensorId +
               ", value=" + value + ", timestamp=" + timestamp + "]";
    }
}
