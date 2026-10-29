package com.warehouse.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.warehouse.model.Priority;
import com.warehouse.model.Robot;
import com.warehouse.model.RobotStatus;
import com.warehouse.model.Task;
import com.warehouse.model.TaskStatus;

// Priority-based task allocation. Two separate questions are answered here:
//   1. Which TASK should be served first?  -> calculateScore() + orderTasks()
//   2. Which ROBOT should do that task?    -> calculateCost() + allocate()
public class PriorityTaskAllocator {

    // A robot below this battery level is not sent out at all,
    // otherwise it could die halfway through a delivery.
    private static final int MIN_BATTERY = 20;

    // A deadline this close (in minutes) earns a bonus.
    private static final int DEADLINE_WINDOW = 10;

    // ---------------------------------------------------------------
    // PART 1: ordering the tasks
    // ---------------------------------------------------------------

    // score = priorityWeight + minutesWaiting * 2 + (deadline within 10 min ? 50 : 0)
    // A HIGHER score means the task is served EARLIER.
    //
    // WHY each part exists:
    //  - priorityWeight: urgent work must beat unimportant work.
    //  - minutesWaiting * 2 ("aging"): every minute a task waits, its score grows.
    //    Without this, a LOW task could wait forever while new URGENT ones keep
    //    arriving (starvation). Aging guarantees every task eventually wins.
    //  - deadline bonus: a task about to miss its deadline jumps the queue,
    //    even if its base priority is low.
    //
    // "now" is passed in (not read inside) so that every task in one ordering is
    // scored at the same moment, which keeps the comparison fair.
    public int calculateScore(Task task, long now) {
        int score = priorityWeight(task.getPriority());

        long minutesWaiting = (now - task.getCreatedAt()) / 60000; // 60000 ms = 1 minute
        score += (int) minutesWaiting * 2;

        // Minutes left = allowed time - time already spent waiting.
        // If it is already negative the task is overdue, which also earns the bonus.
        long minutesLeft = task.getDeadlineMinutes() - minutesWaiting;
        if (minutesLeft <= DEADLINE_WINDOW) {
            score += 50;
        }
        return score;
    }

    private int priorityWeight(Priority p) {
        switch (p) {
            case URGENT: return 100;
            case HIGH:   return 70;
            case MEDIUM: return 40;
            default:     return 10; // LOW
        }
    }

    // Puts the tasks in a PriorityQueue and takes them out one by one,
    // so the result is the list ordered from highest score to lowest.
    public List<Task> orderTasks(List<Task> tasks, long now) {
        // The queue always hands out the "smallest" element first, so we compare
        // b with a (reversed) to make the task with the BIGGEST score come out first.
        Comparator<Task> byScoreDescending =
                (a, b) -> Integer.compare(calculateScore(b, now), calculateScore(a, now));

        PriorityQueue<Task> queue = new PriorityQueue<>(byScoreDescending);
        queue.addAll(tasks);

        List<Task> ordered = new ArrayList<>();
        while (!queue.isEmpty()) {
            ordered.add(queue.poll()); // poll() removes the highest-scoring task
        }
        return ordered;
    }

    // ---------------------------------------------------------------
    // PART 2: choosing the robot
    // ---------------------------------------------------------------

    // A robot may be considered only if ALL of these are true.
    // Checking them first means the cost formula never sees a robot that can't do the job.
    public boolean isEligible(Robot robot, Task task) {
        return robot.getStatus() == RobotStatus.IDLE   // free right now
                && robot.canHandle(task)               // strong enough (each robot type decides)
                && robot.getBattery() >= MIN_BATTERY;  // enough charge to go out
    }

    // cost = manhattanDistance(robot -> pickup point) + (100 - battery) * 0.3
    // LOWER cost is better.
    //
    // WHY: distance means the closest robot arrives sooner. The battery term is a
    // small penalty for low charge, so a fresh robot is preferred over a nearly
    // empty one when distances are similar.
    //
    // Manhattan distance (|dx| + |dy|) is used because robots move along
    // grid aisles, not diagonally.
    // Only the trip to the pickup point is counted: the pickup -> drop-off leg is
    // the same for every robot, so it can't change which robot is cheapest.
    public double calculateCost(Robot robot, Task task) {
        int distance = Math.abs(robot.getX() - task.getSourceX())
                     + Math.abs(robot.getY() - task.getSourceY());
        return distance + (100 - robot.getBattery()) * 0.3;
    }

    // Picks the cheapest eligible robot for the task.
    // If one is found, the robot is marked BUSY and the task ASSIGNED, so the next
    // call for another task will not hand out the same robot again.
    // Returns null when no robot qualifies (the task simply stays PENDING).
    public Robot allocate(Task task, List<Robot> robots) {
        Robot best = null;
        double bestCost = 0;

        for (Robot robot : robots) {
            if (!isEligible(robot, task)) {
                continue; // skip robots that can't take this task
            }
            double cost = calculateCost(robot, task);
            if (best == null || cost < bestCost) { // keep the lowest cost seen so far
                best = robot;
                bestCost = cost;
            }
        }

        if (best != null) {
            best.setStatus(RobotStatus.BUSY);
            task.setStatus(TaskStatus.ASSIGNED);
        }
        return best;
    }
}
