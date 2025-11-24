USE exam_project_db;

-- ==========================================
-- TRUNCATE ALL TABLES
-- ==========================================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE time_entry;
TRUNCATE TABLE task_users;
TRUNCATE TABLE project_users;
TRUNCATE TABLE task;
TRUNCATE TABLE project;
TRUNCATE TABLE user_account;
TRUNCATE TABLE project_role;
TRUNCATE TABLE task_status;

SET FOREIGN_KEY_CHECKS = 1;

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
INSERT INTO project_role (role_name)
VALUES ('Projektleder'),
       ('Teammedlem');

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
       (3, 2, 'Login Modul', 'Implementering af sikker login-funktion', '2025-02-05', '2025-04-20');

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
INSERT INTO project_users (project_id, user_id, role_id)
VALUES (1, 1, 1), -- Anna Projektleder
       (1, 2, 2), -- Bjørn Teammedlem
       (1, 4, 2), -- Dan Teammedlem
       (2, 2, 2), -- Bjørn Teammedlem
       (2, 3, 2), -- Carina Teammedlem
       (2, 5, 2), -- Eva Teammedlem
       (3, 1, 1), -- Anna Projektleder
       (3, 2, 2), -- Bjørn Teammedlem
       (4, 3, 2), -- Carina Teammedlem
       (4, 2, 2); -- Bjørn Teammedlem

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
