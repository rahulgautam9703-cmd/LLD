package booking;

import shared.AppException;

public class BookingNotFoundException extends AppException {
    public BookingNotFoundException(String bookingId) {
        super("Booking not found: " + bookingId, "BOOKING_NOT_FOUND");
    }
}
