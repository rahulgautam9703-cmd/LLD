package booking;

import room.Room;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// First-come-first-serve: just take the first available room (ordered by roomId for determinism),
// ignoring how well it fits. Simpler policy; may "waste" a large room on a small meeting.
public class FirstComeFirstServeSelection implements RoomSelectionStrategy {
    @Override
    public Optional<Room> select(List<Room> availableCandidates) {
        return availableCandidates.stream()
                .min(Comparator.comparing(Room::getRoomId));
    }
}
