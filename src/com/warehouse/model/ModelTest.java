package com.warehouse.model;

// Quick check that all model classes compile and behave as expected.
// Run this file as a Java Application.
public class ModelTest {

    public static void main(String[] args) {
        // The variable type is Robot, but the objects are the subclasses (polymorphism).
        Robot light = new LightCarrierRobot(1, "Lite-1", 90, 0, 0);
        Robot heavy = new HeavyLifterRobot(2, "Heavy-1", 80, 5, 5);
        Robot lowBatteryHeavy = new HeavyLifterRobot(3, "Heavy-2", 20, 2, 2);

        long now = System.currentTimeMillis();
        Task small = new Task(1, "Move parcel", Priority.LOW, 5, 1, 1, 8, 3, now, 60);
        Task big = new Task(2, "Move pallet", Priority.URGENT, 40, 2, 2, 9, 9, now, 15);

        System.out.println("--- Robots ---");
        System.out.println(light);
        System.out.println(heavy);
        System.out.println(lowBatteryHeavy);

        System.out.println("--- Tasks ---");
        System.out.println(small);
        System.out.println(big);

        // Same method call, different answer depending on the robot type.
        System.out.println("--- canHandle ---");
        System.out.println("Lite-1  -> 5kg parcel : " + light.canHandle(small));          // true
        System.out.println("Lite-1  -> 40kg pallet: " + light.canHandle(big));            // false, too heavy
        System.out.println("Heavy-1 -> 40kg pallet: " + heavy.canHandle(big));            // true
        System.out.println("Heavy-2 -> 40kg pallet: " + lowBatteryHeavy.canHandle(big));  // false, battery 20 < 30
    }
}
