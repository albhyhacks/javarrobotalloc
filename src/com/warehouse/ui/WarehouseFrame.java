package com.warehouse.ui;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.warehouse.algorithm.PriorityTaskAllocator;
import com.warehouse.dao.RobotDAO;
import com.warehouse.dao.TaskDAO;
import com.warehouse.model.Robot;
import com.warehouse.model.RobotStatus;
import com.warehouse.model.Task;
import com.warehouse.model.TaskStatus;

// The whole UI in one window:
//   header (title + buttons) / 4 stat cards / robots table / tasks table / allocation log
public class WarehouseFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // Colour palette, used everywhere below.
    static final Color NAVY = new Color(15, 23, 42);
    static final Color BG = new Color(241, 245, 249);
    static final Color CARD = Color.WHITE;
    static final Color MUTED = new Color(100, 116, 139);
    static final Color LINE = new Color(226, 232, 240);
    static final Color TEAL = new Color(13, 148, 136);
    static final Color GREEN = new Color(22, 163, 74);
    static final Color AMBER = new Color(217, 119, 6);
    static final Color RED = new Color(220, 38, 38);
    static final Font FONT = new Font("SansSerif", Font.PLAIN, 13);

    private final PriorityTaskAllocator allocator = new PriorityTaskAllocator();
    private final RobotDAO robotDAO = new RobotDAO();
    private final TaskDAO taskDAO = new TaskDAO();

    private List<Robot> robots = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();

    private final GridPanel gridPanel = new GridPanel();
    private final DefaultTableModel robotModel = newModel("ID", "Name", "Type", "Capacity", "Battery", "Position", "Status");
    private final DefaultTableModel taskModel = newModel("ID", "Description", "Priority", "Weight", "From", "To", "Deadline", "Status", "Score");
    private final JTextArea logArea = new JTextArea();
    private final JLabel idleCount = new JLabel(), busyCount = new JLabel(), pendingCount = new JLabel(), assignedCount = new JLabel();

    public WarehouseFrame() {
        setTitle("Warehouse Robot Task Allocation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1480, 820);
        setLocationRelativeTo(null);

        // --- header: title on the left, buttons on the right ---
        JLabel title = new JLabel("<html><span style='font-size:17pt'><b>Warehouse Control</b></span><br>"
                + "<span style='color:#94a3b8'>Priority-based robot task allocation</span></html>");
        title.setForeground(Color.WHITE);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(button("Reset Demo Data", new Color(51, 65, 85), e -> resetData()));
        buttons.add(button("Complete Tasks  ✓", GREEN, e -> completeAll()));
        buttons.add(button("Run Allocation  ▶", TEAL, e -> runAllocation()));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        header.add(title, BorderLayout.WEST);
        header.add(buttons, BorderLayout.EAST);

        // --- stat cards ---
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(statCard("Idle robots", idleCount, GREEN));
        stats.add(statCard("Busy robots", busyCount, AMBER));
        stats.add(statCard("Pending tasks", pendingCount, RED));
        stats.add(statCard("Assigned tasks", assignedCount, TEAL));

        // --- tables and log ---
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(NAVY);
        logArea.setForeground(new Color(203, 213, 225));
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel tables = new JPanel(new GridLayout(3, 1, 0, 16));
        tables.setOpaque(false);
        tables.add(card("Robots", new JScrollPane(table(robotModel))));
        tables.add(card("Tasks", new JScrollPane(table(taskModel))));
        tables.add(card("Allocation Log", new JScrollPane(logArea)));

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(BG);
        body.setBorder(BorderFactory.createEmptyBorder(16, 24, 24, 24));
        body.add(stats, BorderLayout.NORTH);
        body.add(tables, BorderLayout.CENTER);
        body.add(card("Warehouse Grid", gridPanel), BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        loadData();
    }

    // ---------------------------------------------------------------
    // Small UI builders
    // ---------------------------------------------------------------

    private JButton button(String text, Color color, java.awt.event.ActionListener action) {
        JButton b = new JButton(text);
        b.setFont(FONT.deriveFont(Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(action);
        return b;
    }

    // White panel with a small heading on top and any content below.
    private JPanel card(String heading, JComponent content) {
        JLabel label = new JLabel(heading);
        label.setFont(FONT.deriveFont(Font.BOLD, 14f));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        content.setBorder(BorderFactory.createLineBorder(LINE));

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(12, 14, 14, 14)));
        p.add(label, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    // Card with a coloured stripe on the left, a big number and a caption.
    private JPanel statCard(String caption, JLabel number, Color color) {
        number.setFont(FONT.deriveFont(Font.BOLD, 26f));
        number.setForeground(color);
        JLabel text = new JLabel(caption);
        text.setForeground(MUTED);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, color), BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        p.add(number, BorderLayout.CENTER);
        p.add(text, BorderLayout.SOUTH);
        return p;
    }

    private JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(FONT);
        t.setRowHeight(30);
        t.setShowVerticalLines(false);
        t.setGridColor(LINE);
        t.setSelectionBackground(new Color(204, 251, 241));
        t.setSelectionForeground(NAVY);
        t.setDefaultRenderer(Object.class, new BadgeRenderer());
        t.getTableHeader().setFont(FONT.deriveFont(Font.BOLD, 12f));
        t.getTableHeader().setBackground(BG);
        t.getTableHeader().setForeground(MUTED);
        return t;
    }

    // Display-only table model (cells cannot be edited).
    private static DefaultTableModel newModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    // Colours status and priority words, so the state of each row is visible at a glance.
    static class BadgeRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, sel, focus, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            Color c = switch (String.valueOf(value)) {
                case "IDLE", "ASSIGNED", "COMPLETED", "LOW" -> GREEN;
                case "BUSY", "PENDING", "MEDIUM", "HIGH" -> AMBER;
                case "URGENT", "FAULTY" -> RED;
                default -> null;
            };
            setForeground(c != null ? c : NAVY);
            setFont(c != null ? FONT.deriveFont(Font.BOLD) : FONT);
            return this;
        }
    }

    // ---------------------------------------------------------------
    // Warehouse grid: shows robot positions and task pickup / dropoff points.
    // Green circle = IDLE robot, amber circle = BUSY robot.
    // Filled square = task pickup (amber = pending, teal = assigned).
    // Outlined square = task dropoff (red).
    // ---------------------------------------------------------------
    class GridPanel extends javax.swing.JPanel {
        private static final long serialVersionUID = 1L;
        private static final int CELL = 30;  // pixels per grid square
        private static final int SIZE = 10;  // 10×10 warehouse grid

        GridPanel() {
            setPreferredSize(new java.awt.Dimension(SIZE * CELL, SIZE * CELL));
            setBackground(CARD);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawGrid(g);
            drawTasks(g);
            drawRobots(g);
        }

        private void drawGrid(Graphics g) {
            g.setColor(LINE);
            for (int i = 0; i <= SIZE; i++) {
                g.drawLine(i * CELL, 0, i * CELL, SIZE * CELL);
                g.drawLine(0, i * CELL, SIZE * CELL, i * CELL);
            }
        }

        private void drawTasks(Graphics g) {
            for (Task t : tasks) {
                if (t.getStatus() == TaskStatus.COMPLETED) continue;
                // pickup: filled square, colour shows pending vs assigned
                g.setColor(t.getStatus() == TaskStatus.ASSIGNED ? TEAL : AMBER);
                drawSquare(g, t.getSourceX(), t.getSourceY(), true);
                // dropoff: outlined red square
                g.setColor(RED);
                drawSquare(g, t.getDestX(), t.getDestY(), false);
            }
        }

        private void drawRobots(Graphics g) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 8));
            for (Robot r : robots) {
                g.setColor(r.getStatus() == RobotStatus.IDLE ? GREEN : AMBER);
                drawCircle(g, r.getX(), r.getY());
                g.setColor(NAVY);
                g.drawString(r.getName(), r.getX() * CELL + 2, r.getY() * CELL + CELL - 2);
            }
        }

        // Draws a small square centered in the grid cell at (col, row).
        private void drawSquare(Graphics g, int col, int row, boolean filled) {
            int size = 10;
            int x = col * CELL + (CELL - size) / 2;
            int y = row * CELL + (CELL - size) / 2;
            if (filled) g.fillRect(x, y, size, size);
            else        g.drawRect(x, y, size, size);
        }

        // Draws a circle centered in the grid cell at (col, row).
        private void drawCircle(Graphics g, int col, int row) {
            int size = 14;
            int x = col * CELL + (CELL - size) / 2;
            int y = row * CELL + (CELL - size) / 2;
            g.fillOval(x, y, size, size);
        }
    }

    // ---------------------------------------------------------------
    // Data: load, reset, allocate
    // ---------------------------------------------------------------

    private void loadData() {
        try {
            robots = robotDAO.getAll();
            tasks = taskDAO.getAll();
            refresh();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void resetData() {
        try {
            taskDAO.resetDemoData();
            robotDAO.resetAllToIdle();
            logArea.setText("");
            loadData();
            log("Demo data reset: all robots IDLE, all tasks PENDING.");
        } catch (SQLException e) {
            showError(e);
        }
    }

    // Redraws the tables and stat cards from the robots and tasks lists.
    private void refresh() {
        long now = System.currentTimeMillis();

        robotModel.setRowCount(0);
        for (Robot r : robots) {
            robotModel.addRow(new Object[] { r.getId(), r.getName(), r.getClass().getSimpleName().replace("Robot", ""),
                    r.getCapacity() + " kg", r.getBattery() + "%", "(" + r.getX() + ", " + r.getY() + ")", r.getStatus() });
        }

        taskModel.setRowCount(0);
        for (Task t : tasks) {
            taskModel.addRow(new Object[] { t.getId(), t.getDescription(), t.getPriority(), t.getWeight() + " kg",
                    "(" + t.getSourceX() + ", " + t.getSourceY() + ")", "(" + t.getDestX() + ", " + t.getDestY() + ")",
                    t.getDeadlineMinutes() + " min", t.getStatus(), allocator.calculateScore(t, now) });
        }

        idleCount.setText("" + robots.stream().filter(r -> r.getStatus() == RobotStatus.IDLE).count());
        busyCount.setText("" + robots.stream().filter(r -> r.getStatus() == RobotStatus.BUSY).count());
        pendingCount.setText("" + tasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count());
        assignedCount.setText("" + tasks.stream().filter(t -> t.getStatus() == TaskStatus.ASSIGNED).count());
        gridPanel.repaint();
    }

    // Runs the allocator over all PENDING tasks (highest score first) and saves each decision.
    private void runAllocation() {
        try {
            robots = robotDAO.getAll(); // always allocate against what is really in the database
            tasks = taskDAO.getAll();
            long now = System.currentTimeMillis();
            List<Task> pending = tasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).toList();

            log("\n=== Allocation run: " + pending.size() + " pending task(s) ===");
            for (Task task : allocator.orderTasks(pending, now)) {
                allocateOne(task, now);
            }
            refresh();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void allocateOne(Task task, long now) throws SQLException {
        int score = allocator.calculateScore(task, now);
        long eligible = robots.stream().filter(r -> allocator.isEligible(r, task)).count();
        Robot chosen = allocator.allocate(task, robots);
        String text = "Task " + task.getId() + " \"" + task.getDescription() + "\" [" + task.getPriority() + ", score " + score + "]";

        if (chosen == null) {
            log("  ✗ " + text + " -> NOT ASSIGNED (no idle robot with enough capacity and battery)");
            return;
        }
        double cost = allocator.calculateCost(chosen, task);
        log("  ✓ " + text + " -> " + chosen.getName() + ", cost " + cost + " (best of " + eligible + " eligible)");

        robotDAO.updateStatus(chosen.getId(), RobotStatus.BUSY);
        taskDAO.updateStatus(task.getId(), TaskStatus.ASSIGNED);
        taskDAO.insertAllocation(task.getId(), chosen.getId(), score, cost);
    }

    // Marks every ASSIGNED task COMPLETED and every BUSY robot back to IDLE.
    // Simulates deliveries finishing so new tasks can be allocated.
    private void completeAll() {
        try {
            taskDAO.completeAssigned();
            robotDAO.completeBusy();
            loadData();
            log("All assigned tasks completed. Robots are IDLE again.");
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
    }

    private void showError(SQLException e) {
        log("DATABASE ERROR: " + e.getMessage());
        JOptionPane.showMessageDialog(this, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WarehouseFrame().setVisible(true));
    }
}
