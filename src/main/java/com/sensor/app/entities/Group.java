package com.sensor.app.entities;

import io.vertx.sqlclient.Row;

import java.util.Objects;

public class Group {
    public static final String CREATE_GROUP = "INSERT INTO Group (name, mqtt_channel, home_id, suppressed) VALUES (?, ?, ?, ?)";
    public static final String GET_GROUP_ID = "SELECT * FROM Group WHERE group_id = ?";
    public static final String GET_ALL_GROUP = "SELECT * FROM Group";
    public static final String UPDATE_GROUP = "UPDATE Group SET name = ?, mqtt_channel = ?, home_id = ?, suppressed = ? WHERE group_id = ?";
    public static final String DELETE_GROUP = "DELETE FROM Group WHERE group_id = ?";

    private Integer group_id;
    private String name;
    private String mqttChannel;
    private Integer home_id;
    private Boolean suppressed;

    public Group() {}

    public Group(Row row) {

        setGroup_id(row.getInteger("group_id"));
        setName(row.getString("name"));
        setMqttChannel(row.getString("mqtt_channel"));
        setHome_id(row.getInteger("home_id"));
        setSuppressed(row.getBoolean("suppressed"));

    }

    public Group(int group_id, String name, String mqttChannel) {
        this.group_id = group_id;
        this.name = name;
        this.mqttChannel = mqttChannel;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMqttChannel() {
        return mqttChannel;
    }

    public void setMqttChannel(String mqttChannel) {
        this.mqttChannel = mqttChannel;
    }

    public Integer getHome_id() {
        return home_id;
    }

    public void setHome_id(Integer home_id) {
        this.home_id = home_id;
    }

    public Boolean getSuppressed() {
        return suppressed;
    }


    public void setSuppressed(Boolean suppressed) {
        this.suppressed = suppressed;
    }



    @Override
    public int hashCode() {
        return Objects.hash(group_id, name, mqttChannel);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Group other = (Group) obj;
        return group_id == other.group_id &&
               Objects.equals(name, other.name) &&
               Objects.equals(mqttChannel, other.mqttChannel);
    }

    @Override
    public String toString() {
        return "Group [id=" + group_id + ", name=" + name + ", mqttChannel=" + mqttChannel + "]";
    }
}
