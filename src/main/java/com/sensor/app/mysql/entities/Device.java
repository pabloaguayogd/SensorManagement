package com.sensor.app.mysql.entities;

import java.util.Objects;

public class Device {

    private int id;
    private String name;
    private int groupId;

    public Device() {}

    public Device(int id, String name, int groupId) {
        this.id = id;
        this.name = name;
        this.groupId = groupId;
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

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupId, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Device other = (Device) obj;
        return id == other.id &&
               groupId == other.groupId &&
               Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return "Device [id=" + id + ", name=" + name + ", groupId=" + groupId + "]";
    }
}
