package com.sensor.app.entities;

import io.vertx.sqlclient.Row;

import java.util.Objects;

public class Device {

    public static final String CREATE_DEVICE = "INSERT IGNORE INTO Device (name, group_id) VALUES (?, ?)";
    public static final String GET_DEVICE_ID = "SELECT * FROM Device WHERE device_id = ?";
    public static final String GET_ALL_DEVICE = "SELECT * FROM Device";
    public static final String UPDATE_DEVICE = "UPDATE Device SET name = ?, group_id = ? WHERE device_id = ?";
    public static final String DELETE_DEVICE = "DELETE FROM Device WHERE device_id = ?";

    private int device_id;
    private String name;
    private int group_id;

    public Device() {}

    public Device(Row row) {

        setDevice_id(row.getInteger("device_id"));
        setName(row.getString("name"));
        setGroupId(row.getInteger("group_id"));

    }

    public Device(int device_id, String name, int group_id) {
        this.device_id = device_id;
        this.name = name;
        this.group_id = group_id;
    }

    public int getDevice_id() {
        return device_id;
    }

    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGroupId() {
        return group_id;
    }

    public void setGroupId(int groupId) {
        this.group_id = groupId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(device_id, group_id, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Device other = (Device) obj;
        return device_id == other.device_id &&
               group_id == other.group_id &&
               Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return "Device [id=" + device_id + ", name=" + name + ", group_id=" + group_id + "]";
    }
}
