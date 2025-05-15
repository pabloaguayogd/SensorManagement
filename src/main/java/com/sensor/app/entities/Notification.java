package com.sensor.app.entities;

import java.time.LocalDateTime;

public class Notification {

    private Integer notification_id;
    private LocalDateTime timestamp;
    private String message;
    private Boolean has_read;
    private Integer user_id;


    public Integer getUser_id() {
        return user_id;
    }

    public Boolean getHas_read() {
        return has_read;
    }

    public Integer getNotification_id() {
        return notification_id;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public void setHas_read(Boolean has_read) {
        this.has_read = has_read;
    }

    public void setNotification_id(Integer notification_id) {
        this.notification_id = notification_id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
