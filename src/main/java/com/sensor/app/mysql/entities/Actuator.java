package com.sensor.app.mysql.entities;

import java.util.Objects;

public class Actuator {
    private int id;
    private String name;
    private String type;
    private String identifier;
    private int deviceId;

    public Actuator() {}


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
        return Objects.hash(id, deviceId, identifier, name, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Actuator other = (Actuator) obj;
        return id == other.id &&
               deviceId == other.deviceId &&
               Objects.equals(identifier, other.identifier) &&
               Objects.equals(name, other.name) &&
               Objects.equals(type, other.type);
    }

    @Override
    public String toString() {
        return "Actuator [id=" + id + ", name=" + name + ", type=" + type +
               ", identifier=" + identifier + ", deviceId=" + deviceId + "]";
    }
}
