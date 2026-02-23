-- ===============================================
-- 1. Add hotel_id column (nullable first)
-- ===============================================

ALTER TABLE
    room_inventory
ADD COLUMN
    hotel_id UUID;

-- ===============================================
-- 2. Backfill hotel_id from room_type
-- ===============================================

UPDATE
    room_inventory ri
SET
    hotel_id = rt.hotel_id
FROM
    room_types rt
WHERE
    ri.room_type_id = rt.id;

-- ===============================================
-- 3. Enforce NOT NULL
-- ===============================================

ALTER TABLE
    room_inventory
ALTER COLUMN
    hotel_id SET NOT NULL;

-- ===============================================
-- 4. Add foreign key
-- ===============================================

ALTER TABLE
    room_inventory
ADD CONSTRAINT
    room_inventory_hotel_fk
FOREIGN KEY (hotel_id) REFERENCES hotels(id);

-- ===============================================
-- 5. Add availability search index
-- ===============================================

CREATE INDEX
    inventory_hotel_date_room_type_idx
ON room_inventory (hotel_id, date, room_type_id);