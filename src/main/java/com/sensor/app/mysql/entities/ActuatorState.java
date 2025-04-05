package com.sensor.app.mysql.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class ActuatorState {
    private int id;
    private int actuatorId;
    private boolean state;
    private LocalDateTime timestamp;

    public ActuatorState() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        return Objects.hash(id, actuatorId, state, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ActuatorState other = (ActuatorState) obj;
        return id == other.id &&
               actuatorId == other.actuatorId &&
               state == other.state &&
               Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return "ActuatorState [id=" + id + ", actuatorId=" + actuatorId +
               ", state=" + state + ", timestamp=" + timestamp + "]";
    }
}
