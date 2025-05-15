package com.sensor.app.entities;

import io.vertx.sqlclient.Row;

import java.time.LocalDateTime;
import java.util.Objects;

public class AlarmState {

    public static final String CREATE_ALARM_STATE = "INSERT INTO Alarm_state (actuator_id, state, timestamp) VALUES (?, ?, ?)";
    public static final String GET_ALARM_STATE_ID = "SELECT * FROM Alarm_state WHERE actuator_id = ?";



    private int alarm_state_id;
    private int actuatorId;
    private boolean state;
    private LocalDateTime timestamp;

    public AlarmState() {}

    public AlarmState(Row row) {

        setAlarm_state_id(row.getInteger("id"));
        setActuatorId(row.getInteger("actuator_id"));
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
        return actuatorId;
    }

    public void setActuatorId(int actuatorId) {
        this.actuatorId = actuatorId;
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
        return Objects.hash(alarm_state_id, actuatorId, state, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AlarmState other = (AlarmState) obj;
        return alarm_state_id == other.alarm_state_id &&
               actuatorId == other.actuatorId &&
               state == other.state &&
               Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return "ActuatorState [id=" + alarm_state_id + ", actuatorId=" + actuatorId +
               ", state=" + state + ", timestamp=" + timestamp + "]";
    }
}
