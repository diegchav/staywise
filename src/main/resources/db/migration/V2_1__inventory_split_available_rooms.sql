-- ===============================================
-- 1. Add new capacity columns (nullable for now)
-- ===============================================

ALTER TABLE
    room_inventory
ADD COLUMN
    total_rooms INT,
ADD COLUMN
    reserved_rooms INT DEFAULT 0;

-- ===============================================
-- 2. Migrate existing data
-- ===============================================
-- Since available_rooms represented "current free rooms"
-- and we do not store historical reservations,
-- we assume current free rooms = total capacity.

UPDATE
    room_inventory
SET
    total_rooms = available_rooms,
    reserved_rooms = 0;

-- ===============================================
-- 3. Enforce NOT NULL after migration
-- ===============================================

ALTER TABLE
    room_inventory
ALTER COLUMN
    total_rooms SET NOT NULL,
ALTER COLUMN
      reserved_rooms SET NOT NULL;

-- ===============================================
-- 4. Drop old column
-- ===============================================

ALTER TABLE
    room_inventory
DROP COLUMN
     available_rooms;