package com.sensor.app.entities;

import io.vertx.sqlclient.Row;

import java.util.Objects;

public class Alarm {
    public static final String CREATE_ALARM = "INSERT INTO Alarm (name, type, identifier, device_id) VALUES (?, ?, ?, ?)";
    public static final String GET_ALARM_ID = "SELECT * FROM Alarm WHERE id = ?";
    public static final String GET_ALL_ALARM = "SELECT * FROM Alarm";
    public static final String UPDATE_ALARM = "UPDATE Alarm SET name = ?, type = ?, identifier = ?, device_id = ? WHERE id = ?";
    public static final String DELETE_ALARM = "DELETE FROM Alarm WHERE id = ?";

    private int alarm_id;
    private String name;
    private String type;
    private String identifier;
    private int deviceId;

    public Alarm() {}

    public Alarm(Row row) {

        setAlarm_id(row.getInteger("id"));
        setName(row.getString("name"));
        setType(row.getString("type"));
        setIdentifier(row.getString("identifier"));
        setDeviceId(row.getInteger("device_id"));

    }

    public int getAlarm_id() {
        return alarm_id;
    }

    public void setAlarm_id(int alarm_id) {
        this.alarm_id = alarm_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(alarm_id, deviceId, identifier, name, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Alarm other = (Alarm) obj;
        return alarm_id == other.alarm_id &&
               deviceId == other.deviceId &&
               Objects.equals(identifier, other.identifier) &&
               Objects.equals(name, other.name) &&
               Objects.equals(type, other.type);
    }

    @Override
    public String toString() {
        return "Actuator [id=" + alarm_id + ", name=" + name + ", type=" + type +
               ", identifier=" + identifier + ", deviceId=" + deviceId + "]";
    }
}
