DROP
    DATABASE IF EXISTS exam_project_db;
CREATE
    DATABASE exam_project_db;
USE
    exam_project_db;

CREATE TABLE user_account
(
    user_id       INT          NOT NULL AUTO_INCREMENT,
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
    project_id        INT          NOT NULL AUTO_INCREMENT,
    owner_id          INT          NOT NULL,
    parent_project_id INT          NULL,
    title             VARCHAR(100) NOT NULL,
    description       TEXT         NOT NULL,
    start_date        DATE         NOT NULL,
    end_date          DATE         NOT NULL,
    PRIMARY KEY (project_id),
    CONSTRAINT fk_project_parent_project_id FOREIGN KEY (parent_project_id)
        REFERENCES project (project_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE task_status
(
    status_id   INT         NOT NULL,
    status_name VARCHAR(30) NOT NULL,
    PRIMARY KEY (status_id)
);

CREATE TABLE task
(
    task_id         INT          NOT NULL AUTO_INCREMENT,
    parent_task_id  INT          NULL,
    project_id      INT          NOT NULL,
    title           VARCHAR(100) NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    description     TEXT         NOT NULL,
    estimated_hours DECIMAL      NOT NULL,
    status_id       INT          NOT NULL,
    PRIMARY KEY (task_id),
    CONSTRAINT fk_task_parent_task_id FOREIGN KEY (parent_task_id)
        REFERENCES task (task_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_task_project_id FOREIGN KEY (project_id)
        REFERENCES project (project_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_task_status_id FOREIGN KEY (status_id)
        REFERENCES task_status (status_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


CREATE TABLE time_entry
(
    time_entry_id INT          NOT NULL AUTO_INCREMENT,
    task_id       INT          NOT NULL,
    user_id       INT          NOT NULL,
    hours_worked  DECIMAL      NOT NULL,
    description   VARCHAR(150) NULL,
    PRIMARY KEY (time_entry_id),
    CONSTRAINT fk_time_entry_task_id FOREIGN KEY (task_id)
        REFERENCES task (task_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_time_entry_user_id FOREIGN KEY (user_id)
        REFERENCES user_account (user_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE project_role
(
    role_id   INT          NOT NULL AUTO_INCREMENT,
    role_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (role_id)
);

CREATE TABLE project_users
(
    project_id INT NOT NULL,
    user_id    INT NOT NULL,
    role_id    INT NOT NULL,
    PRIMARY KEY (project_id, user_id),
    CONSTRAINT fk_project_users_project_id FOREIGN KEY (project_id)
        REFERENCES project (project_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_project_users_user_id FOREIGN KEY (user_id)
        REFERENCES user_account (user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_project_users_role_id FOREIGN KEY (role_id)
        REFERENCES project_role (role_id)
        ON DELETE RESTRICT
);

CREATE TABLE task_users
(
    task_id INT NOT NULL,
    user_id INT NOT NULL,
    PRIMARY KEY (task_id, user_id),
    CONSTRAINT fk_task_users_task_id FOREIGN KEY (task_id)
        REFERENCES task (task_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_task_users_user_id FOREIGN KEY (user_id)
        REFERENCES user_account (user_id)
        ON DELETE CASCADE
);