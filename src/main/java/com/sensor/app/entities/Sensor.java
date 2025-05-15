package com.sensor.app.entities;

import io.vertx.sqlclient.Row;

import java.util.Objects;

public class Sensor {

    public static final String CREATE_SENSOR = "INSERT INTO Sensor (name, type, identifier, device_id) VALUES (?, ?, ?, ?)";
    public static final String GET_SENSOR_ID = "SELECT * FROM Sensor WHERE id = ?";
    public static final String GET_ALL_SENSOR = "SELECT * FROM Sensor";
    public static final String UPDATE_SENSOR = "UPDATE Sensor SET name = ?, type = ?, identifier = ?, device_id = ? WHERE id = ?";
    public static final String DELETE_SENSOR = "DELETE FROM Sensor WHERE id = ?";


    private int sensor_id;
    private String name;
    private String type;
    private String identifier;
    private int deviceId;

    public Sensor() {}

    public Sensor(Row row){
        setSensor_id(row.getInteger("id"));
        setName(row.getString("name"));
        setType(row.getString("type"));
        setIdentifier(row.getString("identifier"));
        setDeviceId(row.getInteger("device_id"));
    }

    public Sensor(int sensor_id, String name, String type, String identifier, int deviceId) {
        this.sensor_id = sensor_id;
        this.name = name;
        this.type = type;
        this.identifier = identifier;
        this.deviceId = deviceId;
    }

    public int getSensor_id() {
        return sensor_id;
    }

    public void setSensor_id(int sensor_id) {
        this.sensor_id = sensor_id;
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
        return Objects.hash(sensor_id, name, type, identifier, deviceId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sensor other = (Sensor) obj;
        return sensor_id == other.sensor_id &&
               deviceId == other.deviceId &&
               Objects.equals(name, other.name) &&
               Objects.equals(type, other.type) &&
               Objects.equals(identifier, other.identifier);
    }

    @Override
    public String toString() {
        return "Sensor [id=" + sensor_id + ", name=" + name + ", type=" + type +
               ", identifier=" + identifier + ", deviceId=" + deviceId + "]";
    }
}
