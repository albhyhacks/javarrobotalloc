package com.warehouse.model;

// Big, slow robot for heavy loads.
public class HeavyLifterRobot extends Robot {

    private static final double CAPACITY = 50.0; // kg
    private static final double SPEED = 1.0;     // grid cells per second
    private static final int MIN_BATTERY = 30;   // lifting heavy loads drains the battery faster

    public HeavyLifterRobot(int id, String name, int battery, int x, int y) {
        super(id, name, CAPACITY, battery, x, y);
    }

    // Different rule from LightCarrierRobot: weight must fit AND the battery
    // must be healthy enough for heavy work. Same method, different behaviour.
    @Override
    public boolean canHandle(Task t) {
        return t.getWeight() <= getCapacity() && getBattery() >= MIN_BATTERY;
    }

    @Override
    public double getSpeed() {
        return SPEED;
    }
}
