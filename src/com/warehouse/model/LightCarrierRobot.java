package com.warehouse.model;

// Small, fast robot for light parcels.
public class LightCarrierRobot extends Robot {

    private static final double CAPACITY = 10.0; // kg
    private static final double SPEED = 2.0;     // grid cells per second

    // Capacity is fixed for this robot type, so it is not a constructor parameter.
    public LightCarrierRobot(int id, String name, int battery, int x, int y) {
        super(id, name, CAPACITY, battery, x, y);
    }

    @Override
    public boolean canHandle(Task t) {
        return t.getWeight() <= getCapacity();
    }

    @Override
    public double getSpeed() {
        return SPEED;
    }
}
