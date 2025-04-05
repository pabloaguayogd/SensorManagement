package com.sensor.app.mysql.entities;

import java.util.Objects;

public class Group {
    private int id;
    private String name;
    private String mqttChannel;

    public Group() {}

    public Group(int id, String name, String mqttChannel) {
        this.id = id;
        this.name = name;
        this.mqttChannel = mqttChannel;
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

    public String getMqttChannel() {
        return mqttChannel;
    }

    public void setMqttChannel(String mqttChannel) {
        this.mqttChannel = mqttChannel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, mqttChannel);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Group other = (Group) obj;
        return id == other.id &&
               Objects.equals(name, other.name) &&
               Objects.equals(mqttChannel, other.mqttChannel);
    }

    @Override
    public String toString() {
        return "Group [id=" + id + ", name=" + name + ", mqttChannel=" + mqttChannel + "]";
    }
}
