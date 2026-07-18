import booking.Booking;
import booking.BestFitSelection;
import booking.BookingRequest;
import booking.BookingService;
import booking.RoomSelectionStrategy;
import booking.TimeSlot;
import notification.CommunicationPreference;
import room.Amenity;
import room.Room;
import room.RoomService;
import shared.AppException;
import user.UserService;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

// Composition root: builds the object graph (wiring) and drives a demo booking.
public class MeetingSchedulerClient {

    // small helper so tests read cleanly; all demo bookings are on the same day (2026-07-16)
    private static TimeSlot slot(int startHour, int startMin, int endHour, int endMin) {
        return new TimeSlot(
                LocalDateTime.of(2026, 7, 16, startHour, startMin),
                LocalDateTime.of(2026, 7, 16, endHour, endMin));
    }

    public static void main(String[] args) {
        System.out.println("Welcome to the Meeting Scheduler App!");

        // 1. Client can create rooms (natural id, admin-seeded infra). Builder gives labeled, validated construction.
        RoomService roomService = new RoomService();
        roomService.addRoom(Room.builder("R1", 5).name("Orion").withWhiteboard().build());
        roomService.addRoom(Room.builder("R2", 10).name("Nebula").withProjector().withVideoConf().withAC().build());
        roomService.addRoom(Room.builder("R3", 3).name("Pulsar").withProjector().build());

        // 2. UserService assigns the id (client can't `new User(...)` — package-private ctor).
        UserService userService = new UserService();
        String aliceId = userService.register("Alice", "alice@corp.com", "111");
        String bobId = userService.register("Bob", "bob@corp.com", "222");

        // 3. BookingService gets RoomService + UserService + a RoomSelectionStrategy, all injected (DI).
        //    Swap to FirstComeFirstServeSelection here without touching BookingService.
        RoomSelectionStrategy strategy = new BestFitSelection();
        BookingService bookingService = new BookingService(roomService, userService, strategy);

        // 4. Alice & Bob OPT IN to notifications with their channels: Alice=EMAIL, Bob=EMAIL+SMS.
        //    Subscribing is separate from attending (see TEST 8 with Carol).
        bookingService.subscribeToNotifications(userService.findById(aliceId), EnumSet.of(CommunicationPreference.EMAIL));
        bookingService.subscribeToNotifications(userService.findById(bobId), EnumSet.of(CommunicationPreference.EMAIL, CommunicationPreference.SMS));

        // 5a. Case 1 — a SPECIFIC room (R1). Note: time is a TimeSlot, not raw start/end.
        Booking b1 = bookingService.createBooking(
                BookingRequest.forRoom("R1", List.of(aliceId, bobId), slot(10, 0, 11, 0)));
        System.out.println("Booking " + b1.getId() + " confirmed in specific room " + b1.getRoomId());

        // 5b. Case 2/3 — ANY room that fits 2 and has a PROJECTOR. Best-fit picks R3 (cap 3) over R2 (cap 10).
        Booking b2 = bookingService.createBooking(
                BookingRequest.forAmenities(EnumSet.of(Amenity.PROJECTOR), List.of(aliceId, bobId), slot(14, 0, 15, 0)));
        Room picked = roomService.findById(b2.getRoomId());
        System.out.println("Booking " + b2.getId() + " confirmed in auto-selected room " + b2.getRoomId()
                + " -> " + picked.description() + " @ " + picked.hourlyCost() + "/hr");

        // ---- inline test cases exercising the rules ----

        // TEST 1: same attendee, overlapping time, different room -> ATTENDEE_UNAVAILABLE.
        try {
            bookingService.createBooking(BookingRequest.forRoom("R1", List.of(aliceId), slot(14, 30, 15, 30)));
            System.out.println("TEST1 FAIL: expected attendee clash to be rejected");
        } catch (AppException e) {
            System.out.println("TEST1 OK: rejected double-booked attendee (" + e.getErrorCode() + ")");
        }

        // TEST 2: empty attendee list -> IllegalArgumentException at construction (STRUCTURAL check now).
        try {
            BookingRequest.forRoom("R1", List.of(), slot(16, 0, 17, 0));
            System.out.println("TEST2 FAIL: expected empty attendees to be rejected");
        } catch (IllegalArgumentException e) {
            System.out.println("TEST2 OK: rejected empty attendees (" + e.getMessage() + ")");
        }

        // TEST 3: outside operating hours (before 10:00) -> OUTSIDE_OPERATING_HOURS.
        try {
            bookingService.createBooking(BookingRequest.forRoom("R1", List.of(aliceId), slot(8, 0, 9, 0)));
            System.out.println("TEST3 FAIL: expected out-of-hours booking to be rejected");
        } catch (AppException e) {
            System.out.println("TEST3 OK: rejected out-of-hours booking (" + e.getErrorCode() + ")");
        }

        // TEST 4: blank roomId -> IllegalArgumentException at construction.
        try {
            BookingRequest.forRoom("  ", List.of(aliceId), slot(10, 0, 11, 0));
            System.out.println("TEST4 FAIL: expected blank roomId to be rejected");
        } catch (IllegalArgumentException e) {
            System.out.println("TEST4 OK: rejected blank roomId (" + e.getMessage() + ")");
        }

        // TEST 5: invalid time window (null start) -> IllegalArgumentException from TimeSlot construction.
        try {
            new TimeSlot(null, LocalDateTime.of(2026, 7, 16, 11, 0));
            System.out.println("TEST5 FAIL: expected null start time to be rejected");
        } catch (IllegalArgumentException e) {
            System.out.println("TEST5 OK: rejected null start time (" + e.getMessage() + ")");
        }

        // TEST 6: null amenities set -> IllegalArgumentException (empty set is fine, null is not).
        try {
            BookingRequest.forAmenities(null, List.of(aliceId), slot(10, 0, 11, 0));
            System.out.println("TEST6 FAIL: expected null amenities to be rejected");
        } catch (IllegalArgumentException e) {
            System.out.println("TEST6 OK: rejected null amenities (" + e.getMessage() + ")");
        }

        // TEST 7: attendee is NOT a real user (ghost id) -> USER_NOT_FOUND (referential integrity).
        try {
            bookingService.createBooking(BookingRequest.forRoom("R1", List.of("U404"), slot(18, 0, 19, 0)));
            System.out.println("TEST7 FAIL: expected ghost attendee to be rejected");
        } catch (AppException e) {
            System.out.println("TEST7 OK: rejected ghost attendee (" + e.getErrorCode() + ")");
        }

        // TEST 8: real user who did NOT subscribe -> booking SUCCEEDS, not notified (attendee != observer).
        String carolId = userService.register("Carol", "carol@corp.com", "333"); // real user, not subscribed
        Booking b3 = bookingService.createBooking(BookingRequest.forRoom("R1", List.of(carolId), slot(18, 0, 19, 0)));
        System.out.println("TEST8 OK: booked " + b3.getId() + " for non-subscriber Carol (no notification sent to her)");

        // TEST 9: cancel -> subscribers get a CANCELLED notification (Bob on EMAIL + SMS), slot frees up.
        System.out.println("-- cancelling " + b1.getId() + " (expect CANCELLED notifications) --");
        bookingService.cancelBooking(b1.getId());

        // TEST 10: after cancel, R1 10:00-11:00 is free -> re-booking the same slot succeeds.
        Booking b4 = bookingService.createBooking(BookingRequest.forRoom("R1", List.of(aliceId), slot(10, 0, 11, 0)));
        System.out.println("TEST10 OK: re-booked freed slot as " + b4.getId());

        // TEST 11: cancelling an unknown booking -> BOOKING_NOT_FOUND.
        try {
            bookingService.cancelBooking("B999");
            System.out.println("TEST11 FAIL: expected unknown booking to be rejected");
        } catch (AppException e) {
            System.out.println("TEST11 OK: rejected unknown booking cancel (" + e.getErrorCode() + ")");
        }

        // ---- EDGE CASES ----

        // TEST 12: adjacent slots (10-11 then 11-12) do NOT overlap -> both succeed (boundary of overlap logic).
        Booking e1 = bookingService.createBooking(BookingRequest.forRoom("R2", List.of(bobId), slot(10, 0, 11, 0)));
        Booking e2 = bookingService.createBooking(BookingRequest.forRoom("R2", List.of(bobId), slot(11, 0, 12, 0)));
        System.out.println("TEST12 OK: adjacent same-room slots allowed (" + e1.getId() + ", " + e2.getId() + ")");

        // TEST 13: booking ending EXACTLY at CLOSE (21:00-22:00) is allowed (operating-hours boundary).
        Booking e3 = bookingService.createBooking(BookingRequest.forRoom("R2", List.of(bobId), slot(21, 0, 22, 0)));
        System.out.println("TEST13 OK: booking ending exactly at 22:00 allowed (" + e3.getId() + ")");
    }
}
