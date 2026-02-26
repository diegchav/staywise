-- ===============================================
-- 1. Add address column (nullable)
-- ===============================================

ALTER TABLE
    hotels
ADD COLUMN
    address TEXT;

-- ===============================================
-- 2. Set address to 'Unknown' for existing rows
-- ===============================================

UPDATE
    hotels
SET
    address = 'Unknown'
WHERE
    address IS NULL;

-- ===============================================
-- 3. Enforce NOT NUll
-- ===============================================

ALTER TABLE
    hotels
ALTER COLUMN
    address SET NOT NULL;