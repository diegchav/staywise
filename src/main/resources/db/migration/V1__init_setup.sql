-- Hotels

CREATE TABLE IF NOT EXISTS hotels (
    id              UUID PRIMARY KEY,
    name            TEXT NOT NULL,
    city            TEXT NOT NULL,
    country         TEXT NOT NULL,
    rating          NUMERIC(2,1),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Room types

CREATE TABLE IF NOT EXISTS room_types (
    id              UUID PRIMARY KEY,
    hotel_id        UUID NOT NULL,
    name            TEXT NOT NULL,
    capacity        INT NOT NULL,
    total_rooms     INT NOT NULL,
    base_price      NUMERIC(10, 2) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT room_types_valid_total_rooms
    CHECK (total_rooms > 0),

    CONSTRAINT room_types_hotel_fk
    FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);

-- Room inventory

CREATE TABLE IF NOT EXISTS room_inventory (
    room_type_id    UUID NOT NULL,
    date            DATE NOT NULL,
    available_rooms INT NOT NULL,

    CONSTRAINT room_inventory_positive_available_rooms
    CHECK (available_rooms >= 0),

    CONSTRAINT room_inventory_type_fk
    FOREIGN KEY (room_type_id) REFERENCES room_types(id),

    PRIMARY KEY (room_type_id, date)
);

-- Bookings

CREATE TABLE IF NOT EXISTS bookings (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL,
    hotel_id        UUID NOT NULL,
    room_type_id    UUID NOT NULL,
    check_in        DATE NOT NULL,
    check_out       DATE NOT NULL,
    status          TEXT NOT NULL,
    total_price     NUMERIC(10,2) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT bookings_valid_date_range
    CHECK (check_out > check_in),

    CONSTRAINT bookings_hotel_fk
    FOREIGN KEY (hotel_id) REFERENCES hotels(id),

    CONSTRAINT bookings_room_type_fk
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
);