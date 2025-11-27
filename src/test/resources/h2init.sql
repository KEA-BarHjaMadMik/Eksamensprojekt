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
    role      VARCHAR(20)  NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (role)
);

CREATE TABLE project_users
(
    project_id INT         NOT NULL,
    user_id    INT         NOT NULL,
    role       VARCHAR(20) NOT NULL,
    PRIMARY KEY (project_id, user_id),
    CONSTRAINT fk_project_users_project_id FOREIGN KEY (project_id)
        REFERENCES project (project_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_project_users_user_id FOREIGN KEY (user_id)
        REFERENCES user_account (user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_project_users_role_id FOREIGN KEY (role)
        REFERENCES project_role (role)
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

-- ===============================
-- Task Statuses
-- ===============================
INSERT INTO task_status (status_id, status_name)
VALUES (1, 'Planlægning'),
       (2, 'Klar'),
       (3, 'I gang'),
       (4, 'Færdig');

-- ===============================
-- Project Roles (simplified)
-- ===============================
INSERT INTO project_role (role, role_name)
VALUES ('OWNER','Ejer'),
       ('FULL_ACCESS','Komplet'),
       ('EDIT', 'Rediger'),
       ('READ_ONLY','Se kun');

-- ===============================
-- Users (password set to hash value of 'test123')
-- ===============================
INSERT INTO user_account (email, password_hash, name, title, external)
VALUES ('anna@example.dk', '$2a$10$2fiuXXXrshmlXie3QHLl0Oaa0tM1Suq9AJr3iYIZ5.CNFtZ55VmNS', 'Anna Jensen', 'Teamleder', FALSE),
       ('bjorn@example.dk', '$2a$10$2fiuXXXrshmlXie3QHLl0Oaa0tM1Suq9AJr3iYIZ5.CNFtZ55VmNS', 'Bjørn Nielsen', 'Udvikler', FALSE),
       ('carina@example.dk', '$2a$10$2fiuXXXrshmlXie3QHLl0Oaa0tM1Suq9AJr3iYIZ5.CNFtZ55VmNS', 'Carina Hansen', 'Testleder', FALSE),
       ('dan@example.dk', '$2a$10$2fiuXXXrshmlXie3QHLl0Oaa0tM1Suq9AJr3iYIZ5.CNFtZ55VmNS', 'Dan Sørensen', 'Designer', TRUE),
       ('eva@example.dk', '$2a$10$2fiuXXXrshmlXie3QHLl0Oaa0tM1Suq9AJr3iYIZ5.CNFtZ55VmNS', 'Eva Kristensen', NULL, TRUE);

-- ===============================
-- Projects
-- ===============================
INSERT INTO project (owner_id, parent_project_id, title, description, start_date, end_date)
VALUES (1, NULL, 'Website Redesign', 'Komplet redesign af virksomhedens hjemmeside', '2025-01-01', '2025-06-30'),
       (2, NULL, 'Mobilapp Udvikling', 'Udvikling af ny mobilapplikation', '2025-02-01', '2025-08-31'),
       (1, 1, 'Forside Redesign', 'Redesign af virksomhedens forside', '2025-01-10', '2025-03-15'),
       (3, 2, 'Login Modul', 'Implementering af sikker login-funktion', '2025-02-05', '2025-04-20'),
       (1, NULL, 'Firmajulefrokost', 'Planlægning af årets julefrokost', '2025-11-12', '2025-12-17');

-- ===============================
-- Tasks
-- ===============================
INSERT INTO task (parent_task_id, project_id, title, start_date, end_date, description, estimated_hours, status_id)
VALUES (NULL, 1, 'Design Mockups', '2025-01-01', '2025-02-15', 'Opret designmockups for hovedsider', 80, 1),
       (NULL, 1, 'Frontend Implementering', '2025-02-16', '2025-05-01', 'Udvikl frontend komponenter', 200, 3),
       (2, 1, 'Header Komponent', '2025-02-16', '2025-03-01', 'Udvikling af header UI komponent', 20, 3),
       (NULL, 2, 'App Arkitektur', '2025-02-01', '2025-03-15', 'Opsætning af projektarkitektur', 50, 1),
       (NULL, 2, 'API Integration', '2025-03-16', '2025-05-30', 'Integrer API’er med mobilapp', 150, 3),
       (NULL, 4, 'Login UI', '2025-02-05', '2025-02-28', 'Opret login-skærme', 40, 3),
       (NULL, 4, 'Login Backend', '2025-02-10', '2025-04-20', 'Implementer backend login-logik', 80, 2);

-- ===============================
-- Project Users (simplified roles)
-- ===============================
-- Role 1 = Projektleder, Role 2 = Teammedlem
INSERT INTO project_users (project_id, user_id, role)
VALUES (1, 1, 'OWNER'), -- Anna Projektleder
       (1, 2, 'EDIT'), -- Bjørn Teammedlem
       (1, 4, 'EDIT'), -- Dan Teammedlem
       (2, 2, 'EDIT'), -- Bjørn Teammedlem
       (2, 3, 'EDIT'), -- Carina Teammedlem
       (2, 5, 'EDIT'), -- Eva Teammedlem
       (3, 1, 'OWNER'), -- Anna Projektleder
       (3, 2, 'EDIT'), -- Bjørn Teammedlem
       (4, 3, 'EDIT'), -- Carina Teammedlem
       (4, 2, 'EDIT'); -- Bjørn Teammedlem

-- ===============================
-- Task Users
-- ===============================
INSERT INTO task_users (task_id, user_id)
VALUES (1, 1), -- Anna arbejder på Design Mockups
       (2, 2), -- Bjørn på Frontend Implementering
       (3, 2), -- Bjørn på Header Komponent
       (3, 4), -- Dan på Header Komponent
       (4, 2), -- Bjørn på App Arkitektur
       (5, 2), -- Bjørn på API Integration
       (5, 3), -- Carina på API Integration
       (6, 4), -- Dan på Login UI
       (7, 2), -- Bjørn på Login Backend
       (7, 3); -- Carina på Login Backend

-- ===============================
-- Time Entries
-- ===============================
INSERT INTO time_entry (task_id, user_id, hours_worked, description)
VALUES (1, 1, 8.0, 'Første wireframes'),
       (1, 1, 12.0, 'Endelige mockups'),
       (2, 2, 20.0, 'Opsætning af React projekt'),
       (3, 2, 10.0, 'Header komponent basis'),
       (3, 4, 15.0, 'Header designjustering'),
       (5, 2, 30.0, 'API-kald integration'),
       (5, 3, 20.0, 'Test af API-respons'),
       (6, 4, 12.0, 'Login skærm layouts'),
       (7, 2, 25.0, 'Backend login logik'),
       (7, 3, 15.0, 'Test login backend');
