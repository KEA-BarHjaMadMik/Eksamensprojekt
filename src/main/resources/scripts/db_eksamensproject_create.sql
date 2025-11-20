DROP DATABASE IF EXISTS exam_project_db;
CREATE DATABASE exam_project_db;
USE exam_project_db;

CREATE TABLE user
(
    user_id       INT          NOT NULL,
    email         VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    title         VARCHAR(100) NULL,
    external      BOOLEAN      NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT uc_user_email UNIQUE (email)
);

CREATE TABLE project
(
    project_id        INT          NOT NULL,
    parent_project_id INT          NULL,
    title             VARCHAR(100) NOT NULL,
    description       TEXT         NOT NULL,
    start_date        DATE         NOT NULL,
    end_date          DATE         NOT NULL,
    PRIMARY KEY (project_id),
    CONSTRAINT fk_project_parent_project_id FOREIGN KEY (parent_project_id)
        REFERENCES project (project_id)
);

CREATE TABLE task
(
    task_id         INT          NOT NULL,
    parent_task_id  INT          NULL,
    project_id      INT          NOT NULL,
    title           VARCHAR(100) NOT NULL,
    deadline        DATE         NOT NULL,
    description     TEXT         NOT NULL,
    estimated_hours DECIMAL      NOT NULL,
    status_id       INT          NOT NULL,
    PRIMARY KEY (task_id),
    CONSTRAINT fk_task_parent_task_id FOREIGN KEY (parent_task_id)
        REFERENCES task (task_id),
    CONSTRAINT fk_task_project_id FOREIGN KEY (project_id)
        REFERENCES project (project_id),
    CONSTRAINT fk_task_status_id FOREIGN KEY (status_id)
        REFERENCES task_status (status_id)
);

CREATE TABLE task_status
(
    status_id   int         NOT NULL,
    status_name varchar(30) NOT NULL,
    PRIMARY KEY (status_id)
);

CREATE TABLE time_entry
(
    time_entry_id int          NOT NULL,
    task_id       int          NOT NULL,
    user_id       int          NOT NULL,
    hours_worked  decimal      NOT NULL,
    description   varchar(150) NULL,
    PRIMARY KEY (time_entry_id),
    CONSTRAINT fk_time_entry_task_id FOREIGN KEY (task_id)
        REFERENCES task (task_id),
    CONSTRAINT fk_time_entry_user_id FOREIGN KEY (user_id)
        REFERENCES user (user_id)
);

CREATE TABLE project_role
(
    role_id   int          NOT NULL,
    role_name varchar(100) NOT NULL,
    PRIMARY KEY (role_id)
);

CREATE TABLE project_users
(
    project_id int NOT NULL,
    user_id    int NOT NULL,
    role_id    int NOT NULL,
    PRIMARY KEY (project_id, user_id),
    CONSTRAINT fk_project_users_project_id FOREIGN KEY (project_id)
        REFERENCES project (project_id),
    CONSTRAINT fk_project_users_user_id FOREIGN KEY (user_id)
        REFERENCES user (user_id),
    CONSTRAINT fk_project_users_role_id FOREIGN KEY (role_id)
        REFERENCES project_role (role_id)
);

CREATE TABLE task_users
(
    task_id int NOT NULL,
    user_id int NOT NULL,
    PRIMARY KEY (task_id, user_id),
    CONSTRAINT fk_task_users_task_id FOREIGN KEY (task_id)
        REFERENCES task (task_id),
    CONSTRAINT fk_task_users_user_id FOREIGN KEY (user_id)
        REFERENCES user (user_id)
);