-- ============================================
-- BAPS Bal Mandal - Minimal Seed (Admin + OTP Login)
-- Run after Hibernate has created/updated schema (ddl-auto=update)
-- Database: bapsdelhibalmandal
-- Create zones, sabha kshetra, kids, etc. via frontend/API
--
-- Login: POST /auth/request-otp?phone=9876543210 → get OTP from response (dev)
--        POST /auth/login?phone=9876543210&otp=<otp> → get JWT
--
-- ============================================

USE bapsdelhibalmandal;

-- --------------------------------------------
-- 1. ADMIN ROLE + RIGHTS
-- --------------------------------------------
INSERT IGNORE INTO role (role_id, role_name) VALUES (1, 'ADMIN');

INSERT IGNORE INTO role_rights (role_id, role_right) VALUES
(1, 'CREATE_ZONE'),
(1, 'CREATE_ROLE'),
(1, 'CREATE_SABHAKSHETRA'),
(1, 'ADD_KID'),
(1, 'UPDATE_KID'),
(1, 'MAP_STATUS'),
(1, 'MARK_ATTENDANCE'),
(1, 'VIEW_KIDS'),
(1, 'VIEW_ATTENDANCE');

-- --------------------------------------------
-- 2. ADMIN USER (change phone_number to yours)
-- --------------------------------------------
INSERT IGNORE INTO `user` (user_id, full_name, phone_number, is_kid)
VALUES (1, 'Admin User', '9876543210', 0);

INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (1, 1);

-- --------------------------------------------
-- 3. STATUS (required when creating kids - no Status API)
-- --------------------------------------------
INSERT IGNORE INTO status (status_id, status) VALUES (1, 'Active');
INSERT IGNORE INTO status (status_id, status) VALUES (2, 'Inactive');
