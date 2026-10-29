package com.warehouse.model;

// Only an IDLE robot can be given a new task.
public enum RobotStatus {
    IDLE, BUSY, CHARGING, FAULTY
}
