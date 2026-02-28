-- Fix AUTO_INCREMENT for tables that had sequence conflicts
-- Run this in MySQL Workbench if you still get "Duplicate entry" errors after the code fix

USE bapsdelhibalmandal;

-- Ensure sabha_kshetra uses AUTO_INCREMENT (next id = max + 1)
ALTER TABLE sabha_kshetra MODIFY COLUMN kshetra_id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE sabha_kshetra AUTO_INCREMENT = 5;

-- Same for role table (if needed)
ALTER TABLE role MODIFY COLUMN role_id INT NOT NULL AUTO_INCREMENT;
ALTER TABLE role AUTO_INCREMENT = 5;

-- Same for status table (if needed)
ALTER TABLE status MODIFY COLUMN status_id INT NOT NULL AUTO_INCREMENT;
ALTER TABLE status AUTO_INCREMENT = 3;
