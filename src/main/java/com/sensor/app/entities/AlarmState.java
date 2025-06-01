package com.sensor.app.entities;

import io.vertx.sqlclient.Row;

import java.time.LocalDateTime;
import java.util.Objects;

public class AlarmState {

    public static final String CREATE_ALARM_STATE = "INSERT INTO Alarm_state (alarm_id, state, timestamp) VALUES (?, ?, ?)";
    public static final String GET_ALARM_STATE_ID = "SELECT * FROM Alarm_state WHERE alarm_id = ?";
    public static final String GET_ALARM_STATE_IN_GROUP = "SELECT Alarm.name, Alarm_state.alarm_id, Alarm_state.state, Alarm_state.timestamp  FROM Alarm_state INNER JOIN Alarm ON Alarm.alarm_id = Alarm_state.alarm_id INNER JOIN Device ON Alarm.device_id = Device.device_id WHERE group_id = ? ORDER BY Alarm_state.timestamp DESC LIMIT 10 ";



    private int alarm_state_id;
    private int alarm_id;
    private boolean state;
    private LocalDateTime timestamp;

    public AlarmState() {}

    public AlarmState(Row row) {

        setAlarm_state_id(row.getInteger("id"));
        setActuatorId(row.getInteger("alarm_id"));
        setState(row.getBoolean("state"));
        setTimestamp(row.getLocalDateTime("timestamp"));

    }

    public int getAlarm_state_id() {
        return alarm_state_id;
    }

    public void setAlarm_state_id(int alarm_state_id) {
        this.alarm_state_id = alarm_state_id;
    }

    public int getActuatorId() {
        return alarm_id;
    }

    public void setActuatorId(int actuatorId) {
        this.alarm_id = actuatorId;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(alarm_state_id, alarm_id, state, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AlarmState other = (AlarmState) obj;
        return alarm_state_id == other.alarm_state_id &&
               alarm_id == other.alarm_id &&
               state == other.state &&
               Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return "ActuatorState [id=" + alarm_state_id + ", actuator_id=" + alarm_id +
               ", state=" + state + ", timestamp=" + timestamp + "]";
    }
}
