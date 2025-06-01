package com.sensor.app.entities;

import io.vertx.sqlclient.Row;

import java.time.LocalDateTime;

public class Notification {

    public static final String CREATE_NOTIFICATION = "INSERT INTO Notification (timestamp, message, has_read, user_id) VALUES (?, ?, ?, ?)";
    public static final String GET_NOTIFICATION_ID = "SELECT * FROM Notification WHERE notification_id = ?";
    public static final String GET_ALL_NOTIFICATION = "SELECT * FROM Notification";
    public static final String UPDATE_NOTIFICATION = "UPDATE Notification SET timestamp = ?, message = ?, has_read = ?, user_id = ? WHERE notification_id = ?";
    public static final String DELETE_NOTIFICATION = "DELETE FROM Notification WHERE notification_id = ?";


    public Notification(Row row) {

        setTimestamp(row.getLocalDateTime("timestamp"));
        setMessage(row.getString("message"));
        setHas_read(row.getBoolean("has_read"));
        setUser_id(row.getInteger("user_id"));

    }

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
