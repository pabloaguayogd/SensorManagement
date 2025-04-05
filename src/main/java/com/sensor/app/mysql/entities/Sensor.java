package com.sensor.app.mysql.entities;

import java.util.Objects;

public class Sensor {

    private int id;
    private String name;
    private String type;
    private String identifier;
    private int deviceId;

    public Sensor() {}

    public Sensor(int id, String name, String type, String identifier, int deviceId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.identifier = identifier;
        this.deviceId = deviceId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        return Objects.hash(id, name, type, identifier, deviceId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sensor other = (Sensor) obj;
        return id == other.id &&
               deviceId == other.deviceId &&
               Objects.equals(name, other.name) &&
               Objects.equals(type, other.type) &&
               Objects.equals(identifier, other.identifier);
    }

    @Override
    public String toString() {
        return "Sensor [id=" + id + ", name=" + name + ", type=" + type +
               ", identifier=" + identifier + ", deviceId=" + deviceId + "]";
    }
}
