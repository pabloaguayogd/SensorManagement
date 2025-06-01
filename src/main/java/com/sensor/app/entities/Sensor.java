package com.sensor.app.entities;

import io.vertx.sqlclient.Row;

import java.util.Objects;

public class Sensor {

    public static final String CREATE_SENSOR = "INSERT IGNORE INTO Sensor (name, type, device_id) VALUES (?, ?, ?)";
    public static final String GET_SENSOR_ID = "SELECT * FROM Sensor WHERE sensor_id = ?";
    public static final String GET_ALL_SENSOR = "SELECT * FROM Sensor";
    public static final String UPDATE_SENSOR = "UPDATE Sensor SET name = ?, type = ?, device_id = ? WHERE sensor_id = ?";
    public static final String DELETE_SENSOR = "DELETE FROM Sensor WHERE sensor_id = ?";
    public static final String GET_BY_GROUP = "SELECT * FROM Sensor INNER JOIN Device ON Sensor.device_id = Device.device_id INNER JOIN `Group` ON Device.group_id = `Group`.group_id WHERE  `Group`.group_id = ?";


    private int sensor_id;
    private String name;
    private String type;
    private int device_id;

    public Sensor() {}

    public Sensor(Row row){
        setSensor_id(row.getInteger("sensor_id"));
        setName(row.getString("name"));
        setType(row.getString("type"));
        setDeviceId(row.getInteger("device_id"));
    }

    public Sensor(int sensor_id, String name, String type, int device_id) {
        this.sensor_id = sensor_id;
        this.name = name;
        this.type = type;
        this.device_id = device_id;
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

    public int getDeviceId() {
        return device_id;
    }

    public void setDeviceId(int deviceId) {
        this.device_id = deviceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensor_id, name, type, device_id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sensor other = (Sensor) obj;
        return sensor_id == other.sensor_id &&
               device_id == other.device_id &&
               Objects.equals(name, other.name) &&
               Objects.equals(type, other.type);
    }

    @Override
    public String toString() {
        return "Sensor [id=" + sensor_id + ", name=" + name + ", type=" + type + ", device_id=" + device_id + "]";
    }
}
