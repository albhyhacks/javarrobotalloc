package com.warehouse.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.warehouse.model.Priority;
import com.warehouse.model.Task;
import com.warehouse.model.TaskStatus;
import com.warehouse.util.DBConnection;

// All SQL for the tasks table (and the allocations history) lives here.
public class TaskDAO {

    public List<Task> getAll() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        try (ResultSet rs = DBConnection.get().createStatement().executeQuery("SELECT * FROM tasks ORDER BY id")) {
            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("description"),
                        Priority.valueOf(rs.getString("priority")), // text -> enum
                        rs.getDouble("weight"),
                        rs.getInt("source_x"), rs.getInt("source_y"),
                        rs.getInt("dest_x"), rs.getInt("dest_y"),
                        rs.getLong("created_at"),
                        rs.getInt("deadline_minutes"));
                task.setStatus(TaskStatus.valueOf(rs.getString("status")));
                tasks.add(task);
            }
        }
        return tasks;
    }

    public void updateStatus(int taskId, TaskStatus status) throws SQLException {
        DBConnection.run("UPDATE tasks SET status = ? WHERE id = ?", status.name(), taskId);
    }

    // Records one allocation decision in the history table.
    public void insertAllocation(int taskId, int robotId, int score, double cost) throws SQLException {
        DBConnection.run("INSERT INTO allocations (task_id, robot_id, score, cost) VALUES (?, ?, ?, ?)",
                taskId, robotId, score, cost);
    }

    // Minutes each demo task (ids 1 to 4) has already waited. Matches sql/warehouse_schema.sql.
    private static final int[] DEMO_MINUTES_WAITING = {0, 5, 45, 2};

    // Marks all currently-assigned tasks as completed. Used by the Complete button.
    public void completeAssigned() throws SQLException {
        DBConnection.run("UPDATE tasks SET status = 'COMPLETED' WHERE status = 'ASSIGNED'");
    }

    // Puts the tasks back to the start of the demo. Used by the Reset button.
    public void resetDemoData() throws SQLException {
        DBConnection.run("DELETE FROM allocations");          // first: allocations point at tasks
        DBConnection.run("UPDATE tasks SET status = 'PENDING'");

        // Restart each task's waiting clock, otherwise scores keep growing in real time.
        for (int i = 0; i < DEMO_MINUTES_WAITING.length; i++) {
            long createdAt = System.currentTimeMillis() - DEMO_MINUTES_WAITING[i] * 60000L;
            DBConnection.run("UPDATE tasks SET created_at = ? WHERE id = ?", createdAt, i + 1);
        }
    }
}
