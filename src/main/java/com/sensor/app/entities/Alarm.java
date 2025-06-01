package com.sensor.app.entities;

import io.vertx.sqlclient.Row;

import java.util.Objects;

public class Alarm {
    public static final String CREATE_ALARM = "INSERT IGNORE INTO Alarm (name, type, device_id) VALUES (?, ?, ?)";
    public static final String GET_ALARM_ID = "SELECT * FROM Alarm WHERE alarm_id = ?";
    public static final String GET_BY_GROUP = "SELECT * FROM Alarm INNER JOIN Device ON Alarm.device_id = Device.device_id INNER JOIN `Group` ON Device.group_id = `Group`.group_id WHERE  `Group`.group_id = ?";
    public static final String GET_ALL_ALARM = "SELECT * FROM Alarm";
    public static final String UPDATE_ALARM = "UPDATE Alarm SET name = ?, type = ?, device_id = ? WHERE alarm_id = ?";
    public static final String DELETE_ALARM = "DELETE FROM Alarm WHERE alarm_id = ?";

    private int alarm_id;
    private String name;
    private String type;
    private int device_id;

    public Alarm() {}

    public Alarm(Row row) {

        setAlarm_id(row.getInteger("alarm_id"));
        setName(row.getString("name"));
        setType(row.getString("type"));
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

    public int getDeviceId() {
        return device_id;
    }

    public void setDeviceId(int deviceId) {
        this.device_id = deviceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(alarm_id, device_id, name, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Alarm other = (Alarm) obj;
        return alarm_id == other.alarm_id &&
               device_id == other.device_id &&
               Objects.equals(name, other.name) &&
               Objects.equals(type, other.type);
    }

    @Override
    public String toString() {
        return "Actuator [alarm_id=" + alarm_id + ", name=" + name + ", type=" + type + ", device_id=" + device_id + "]";
    }
}
