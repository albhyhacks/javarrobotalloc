package com.warehouse.model;

// Abstract base class: holds what every robot has in common.
// canHandle and getSpeed differ per robot type, so each subclass supplies its own (polymorphism).
public abstract class Robot {

    // private fields + public getters = encapsulation
    private final int id;
    private final String name;
    private final double capacity; // max weight in kg
    private final int battery;     // percentage, 0 to 100
    private final int x, y;        // position on the warehouse grid
    private RobotStatus status = RobotStatus.IDLE; // a new robot starts out free

    public Robot(int id, String name, double capacity, int battery, int x, int y) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.battery = battery;
        this.x = x;
        this.y = y;
    }

    public abstract boolean canHandle(Task t); // can this robot physically do the task?
    public abstract double getSpeed();         // grid cells per second

    public int getId() { return id; }
    public String getName() { return name; }
    public double getCapacity() { return capacity; }
    public int getBattery() { return battery; }
    public int getX() { return x; }
    public int getY() { return y; }
    public RobotStatus getStatus() { return status; }
    public void setStatus(RobotStatus status) { this.status = status; }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ", " + capacity + "kg, battery " + battery
                + "%, pos (" + x + "," + y + "), speed " + getSpeed() + ", " + status + "]";
    }
}
