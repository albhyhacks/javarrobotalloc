package com.warehouse.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.warehouse.model.HeavyLifterRobot;
import com.warehouse.model.LightCarrierRobot;
import com.warehouse.model.Robot;
import com.warehouse.model.RobotStatus;
import com.warehouse.util.DBConnection;

// All SQL for the robots table lives here, so the rest of the program never sees SQL.
public class RobotDAO {

    public List<Robot> getAll() throws SQLException {
        List<Robot> robots = new ArrayList<>();
        try (ResultSet rs = DBConnection.get().createStatement().executeQuery("SELECT * FROM robots ORDER BY id")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int battery = rs.getInt("battery");
                int x = rs.getInt("x");
                int y = rs.getInt("y");

                // The robot_type column decides which subclass to create.
                Robot robot = rs.getString("robot_type").equals("HEAVY")
                        ? new HeavyLifterRobot(id, name, battery, x, y)
                        : new LightCarrierRobot(id, name, battery, x, y);
                robot.setStatus(RobotStatus.valueOf(rs.getString("status"))); // text -> enum
                robots.add(robot);
            }
        }
        return robots;
    }

    public void updateStatus(int robotId, RobotStatus status) throws SQLException {
        DBConnection.run("UPDATE robots SET status = ? WHERE id = ?", status.name(), robotId);
    }

    // Sets only BUSY robots back to IDLE. Used by the Complete button.
    public void completeBusy() throws SQLException {
        DBConnection.run("UPDATE robots SET status = 'IDLE' WHERE status = 'BUSY'");
    }

    // Puts every robot back to IDLE. Used by the Reset button.
    public void resetAllToIdle() throws SQLException {
        DBConnection.run("UPDATE robots SET status = 'IDLE'");
    }
}
