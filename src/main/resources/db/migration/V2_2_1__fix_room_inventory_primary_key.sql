-- ===============================================
-- 1. Drop existing primary key
-- ===============================================

ALTER TABLE
    room_inventory
DROP CONSTRAINT
    room_inventory_pkey;

-- ===============================================
-- 2. Create new composite primary key
-- ===============================================

ALTER TABLE
    room_inventory
ADD CONSTRAINT
    room_inventory_pkey
PRIMARY KEY (hotel_id, room_type_id, date);