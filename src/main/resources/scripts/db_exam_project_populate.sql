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
-- Project Roles
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
VALUES
-- Projekt 1: Website Redesign
(NULL, 1, 'Design Mockups', '2025-01-01', '2025-02-15', 'Opret designmockups for hovedsider', 80, 1),
(1, 1, 'Forside Mockup', '2025-01-01', '2025-01-20', 'Design af forside', 20, 1),
(1, 1, 'Kontaktside Mockup', '2025-01-10', '2025-01-25', 'Design af kontaktside', 15, 1),
(NULL, 1, 'Frontend Implementering', '2025-02-16', '2025-05-01', 'Udvikl frontend komponenter', 200, 3),
(4, 1, 'Header Komponent', '2025-02-16', '2025-03-01', 'Udvikling af header UI komponent', 20, 3),
(4, 1, 'Footer Komponent', '2025-03-02', '2025-03-15', 'Udvikling af footer UI komponent', 15, 2),
(5, 1, 'Logo Dropdown', '2025-02-17', '2025-02-25', 'Dropdown menu for logo', 8, 3),
(NULL, 1, 'Content Management', '2025-03-01', '2025-05-01', 'CMS integration og opsætning', 120, 2),
(8, 1, 'Blog Modul', '2025-03-05', '2025-04-01', 'Opsæt blog modul', 40, 2),
(9, 1, 'Kommentar Funktion', '2025-03-10', '2025-03-20', 'Underopgave til Blog Modul', 10, 1),

-- Projekt 2: Mobilapp Udvikling
(NULL, 2, 'App Arkitektur', '2025-02-01', '2025-03-15', 'Opsætning af projektarkitektur', 50, 1),
(NULL, 2, 'API Integration', '2025-03-16', '2025-05-30', 'Integrer API’er med mobilapp', 150, 3),

-- Projekt 3: Forside Redesign (delprojekt af Website Redesign)
(NULL, 3, 'Redesign Forside', '2025-01-10', '2025-03-15', 'Redesign af forside layout', 30, 2),

-- Projekt 4: Login Modul (delprojekt af Mobilapp Udvikling)
(NULL, 4, 'Login UI', '2025-02-05', '2025-02-28', 'Opret login-skærme', 40, 3),
(NULL, 4, 'Login Backend', '2025-02-10', '2025-04-20', 'Implementer backend login-logik', 80, 2),

-- Projekt 5: Firmajulefrokost
(NULL, 5, 'Lokalebooking', '2025-11-12', '2025-11-20', 'Book lokale til julefrokost', 10, 1),
(NULL, 5, 'Menuplanlægning', '2025-11-21', '2025-12-01', 'Planlæg menu og drikkevarer', 15, 1),
(NULL, 5, 'Invitationer', '2025-11-15', '2025-11-30', 'Send invitationer til medarbejdere', 5, 1);

-- ===============================
-- Project Users (simplified roles)
-- ===============================
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
       (4, 2, 'EDIT'), -- Bjørn Teammedlem
       (5, 1, 'OWNER'); -- Anna Projekleder

-- ===============================
-- Task Users
-- ===============================
INSERT INTO task_users (task_id, user_id)
VALUES (1, 1),
       (2, 1),
       (3, 1),
       (4, 2),
       (5, 2),
       (6, 4),
       (7, 2),
       (8, 1),
       (9, 2),
       (10, 4),
       (11, 2),
       (12, 3),
       (13, 4),
       (14, 2);

-- ===============================
-- Time Entries
-- ===============================
INSERT INTO time_entry (task_id, user_id, hours_worked, description)
VALUES (1, 1, 8.0, 'Første wireframes'),
       (1, 1, 12.0, 'Endelige mockups'),
       (2, 1, 5.0, 'Forside layout'),
       (3, 1, 4.0, 'Kontaktside layout'),
       (4, 2, 20.0, 'Opsætning af React projekt'),
       (5, 2, 10.0, 'Header komponent basis'),
       (6, 4, 15.0, 'Footer designjustering'),
       (7, 2, 8.0, 'Dropdown menu implementering'),
       (8, 1, 25.0, 'CMS opsætning'),
       (9, 2, 12.0, 'Blog modul funktioner'),
       (10, 4, 6.0, 'Kommentar funktion test'),
       (12, 2, 30.0, 'API-kald integration'),
       (12, 3, 20.0, 'Test af API-respons'),
       (13, 4, 12.0, 'Login skærm layouts'),
       (14, 2, 25.0, 'Backend login logik'),
       (14, 3, 15.0, 'Test login backend');
