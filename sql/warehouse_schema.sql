-- Run this whole file in MySQL Workbench (lightning bolt button).
USE warehouse_db;

-- Enum values (status, priority, robot_type) are stored as plain text.
-- The Java DAOs turn the text back into enums with valueOf().

CREATE TABLE IF NOT EXISTS robots (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    robot_type VARCHAR(10) NOT NULL,              -- 'LIGHT' or 'HEAVY' (decides which Java class is built)
    battery    INT         NOT NULL,              -- percentage 0-100
    x          INT         NOT NULL,
    y          INT         NOT NULL,
    status     VARCHAR(10) NOT NULL DEFAULT 'IDLE'  -- IDLE, BUSY, CHARGING, FAULTY
);
-- No capacity column: capacity is fixed by the robot type in the Java classes.

CREATE TABLE IF NOT EXISTS tasks (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    description      VARCHAR(100) NOT NULL,
    priority         VARCHAR(10)  NOT NULL,       -- URGENT, HIGH, MEDIUM, LOW
    weight           DOUBLE       NOT NULL,       -- kg
    source_x         INT NOT NULL,
    source_y         INT NOT NULL,
    dest_x           INT NOT NULL,
    dest_y           INT NOT NULL,
    status           VARCHAR(10)  NOT NULL DEFAULT 'PENDING',  -- PENDING, ASSIGNED, COMPLETED
    created_at       BIGINT       NOT NULL,       -- milliseconds since 1970, same as Java's System.currentTimeMillis()
    deadline_minutes INT          NOT NULL
);

-- One row per allocation decision (history / log).
CREATE TABLE IF NOT EXISTS allocations (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    task_id      INT    NOT NULL,
    robot_id     INT    NOT NULL,
    score        INT    NOT NULL,                 -- task score when it was allocated
    cost         DOUBLE NOT NULL,                 -- robot cost when it was allocated
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id)  REFERENCES tasks(id),
    FOREIGN KEY (robot_id) REFERENCES robots(id)
);

-- ---------------------------------------------------------------
-- Sample data (same scenario as the earlier hard-coded test)
-- ---------------------------------------------------------------
INSERT INTO robots (name, robot_type, battery, x, y, status) VALUES
    ('Lite-1',  'LIGHT', 90, 0, 0, 'IDLE'),
    ('Lite-2',  'LIGHT', 70, 9, 9, 'IDLE'),
    ('Heavy-1', 'HEAVY', 80, 5, 5, 'IDLE'),
    ('Heavy-2', 'HEAVY', 25, 2, 2, 'IDLE');   -- battery too low for heavy work

-- created_at = (now in seconds - minutes waiting * 60) * 1000
INSERT INTO tasks (description, priority, weight, source_x, source_y, dest_x, dest_y, created_at, deadline_minutes) VALUES
    ('Restock shelf',    'LOW',    5,  1, 1, 8, 3, UNIX_TIMESTAMP() * 1000,              60),
    ('Move pallet',      'URGENT', 40, 4, 4, 9, 9, (UNIX_TIMESTAMP() - 5 * 60) * 1000,   30),
    ('Old medium order', 'MEDIUM', 8,  1, 1, 6, 6, (UNIX_TIMESTAMP() - 45 * 60) * 1000,  120),
    ('Express parcel',   'HIGH',   3,  9, 8, 0, 0, (UNIX_TIMESTAMP() - 2 * 60) * 1000,   11);

-- ---------------------------------------------------------------
-- RESET: run these lines to repeat the demo after an allocation has run
-- (allocation marks robots BUSY and tasks ASSIGNED in the database).
-- The last 4 lines restart each task's waiting clock. Without them the tasks
-- keep aging in real time, and the scores drift far above the demo values.
-- ---------------------------------------------------------------
-- DELETE FROM allocations;
-- UPDATE robots SET status = 'IDLE';
-- UPDATE tasks  SET status = 'PENDING';
-- UPDATE tasks SET created_at = (UNIX_TIMESTAMP() -  0 * 60) * 1000 WHERE id = 1;
-- UPDATE tasks SET created_at = (UNIX_TIMESTAMP() -  5 * 60) * 1000 WHERE id = 2;
-- UPDATE tasks SET created_at = (UNIX_TIMESTAMP() - 45 * 60) * 1000 WHERE id = 3;
-- UPDATE tasks SET created_at = (UNIX_TIMESTAMP() -  2 * 60) * 1000 WHERE id = 4;
