-- ============================================
-- Migration: sabha_kshetra from old schema to Zone + SabhaKshetra
-- Run ONLY if you have existing sabha_kshetra with old columns (kshtra, no zone_id)
-- Run statements one by one; skip any that fail (e.g. column already exists)
-- ============================================

USE bapsdelhibalmandal;

-- 1. Create zone table if not exists
CREATE TABLE IF NOT EXISTS zone (
    zone_id BIGINT NOT NULL AUTO_INCREMENT,
    zone_name VARCHAR(255),
    description VARCHAR(500),
    PRIMARY KEY (zone_id)
);

-- 2. Add zone_id to sabha_kshetra (run only if column doesn't exist)
ALTER TABLE sabha_kshetra ADD COLUMN zone_id BIGINT NULL;

-- 3. Add kshetra_name (run only if column doesn't exist)
ALTER TABLE sabha_kshetra ADD COLUMN kshetra_name VARCHAR(255) NULL;

-- 4. Copy kshtra -> kshetra_name
UPDATE sabha_kshetra SET kshetra_name = kshtra WHERE kshetra_name IS NULL AND kshtra IS NOT NULL;

-- 5. Create default zone and link
INSERT IGNORE INTO zone (zone_id, zone_name, description) VALUES (1, 'Default Zone', 'Migrated from legacy');
UPDATE sabha_kshetra SET zone_id = 1 WHERE zone_id IS NULL;

-- 6. Enforce NOT NULL
ALTER TABLE sabha_kshetra MODIFY COLUMN zone_id BIGINT NOT NULL;
ALTER TABLE sabha_kshetra MODIFY COLUMN kshetra_name VARCHAR(255) NOT NULL;

-- 7. Drop old column (run only if kshtra exists)
ALTER TABLE sabha_kshetra DROP COLUMN kshtra;
