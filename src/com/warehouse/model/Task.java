package com.warehouse.model;

// One job: pick a load up at (sourceX, sourceY) and deliver it to (destX, destY).
public class Task {

    private final int id;
    private final String description;
    private final Priority priority;
    private final double weight;        // kg
    private final int sourceX, sourceY;
    private final int destX, destY;
    private final long createdAt;       // milliseconds since 1970 (System.currentTimeMillis())
    private final int deadlineMinutes;  // minutes allowed, counted from createdAt
    private TaskStatus status = TaskStatus.PENDING; // every task starts out waiting

    public Task(int id, String description, Priority priority, double weight,
                int sourceX, int sourceY, int destX, int destY,
                long createdAt, int deadlineMinutes) {
        this.id = id;
        this.description = description;
        this.priority = priority;
        this.weight = weight;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.destX = destX;
        this.destY = destY;
        this.createdAt = createdAt;
        this.deadlineMinutes = deadlineMinutes;
    }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public double getWeight() { return weight; }
    public int getSourceX() { return sourceX; }
    public int getSourceY() { return sourceY; }
    public int getDestX() { return destX; }
    public int getDestY() { return destY; }
    public long getCreatedAt() { return createdAt; }
    public int getDeadlineMinutes() { return deadlineMinutes; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Task[" + id + ", " + description + ", " + priority + ", " + weight + "kg, "
                + "(" + sourceX + "," + sourceY + ") -> (" + destX + "," + destY + "), " + status + "]";
    }
}
