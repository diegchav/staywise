package com.diegchav.staywise.constant;

public class ErrorMessages {
    // Availability.
    public static final String AVAILABILITY_INVALID_DATE_RANGE = "Invalid date range";

    // Booking.
    public static final String BOOKING_NOT_FOUND = "Booking not found";

    // Idempotency.
    public static final String IDEMPOTENCY_RECORD_NOT_FOUND = "Key exists but record not found";

    // Inventory.
    public static final String INVENTORY_EMPTY = "No inventory available";
    public static final String INVENTORY_SOLD_OUT = "Inventory sold out";
    public static final String INVENTORY_RELEASE_FAILED = "Inventory release failed for date: ";

    private ErrorMessages() {}
}
