package booking;

import room.Room;

import java.util.List;
import java.util.Optional;

// Strategy pattern: pluggable policy for choosing ONE room from the already-available candidates.
// Lives in `booking` (not `room`) because "which room to pick for a booking" is a booking/allocation
// POLICY owned by the consumer (BookingService) — not an intrinsic property of a Room. Dependency
// Inversion: the abstraction belongs with the layer that depends on it.
// BookingService does the filtering (capacity + amenities + availability) and delegates only the
// "which of these do we pick?" decision here — so adding a policy (cheapest, closest, ...) is a new
// class, not an edit to BookingService (Open/Closed).
public interface RoomSelectionStrategy {
    Optional<Room> select(List<Room> availableCandidates);
}
