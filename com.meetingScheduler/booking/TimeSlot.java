package booking;

import java.time.LocalDateTime;

// Immutable
// Encapsulates the (easy-to-get-wrong) overlap logic in ONE place instead of duplicating the formula across availability + double-booking checks.
public final class TimeSlot {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public TimeSlot(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end are required");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end: " + start + " .. " + end);
        }
        this.start = start;
        this.end = end;
    }

    // Two intervals overlap when each starts before the other ends: startA < endB && startB < endA.
    // (Adjacent slots like 10-11 and 11-12 do NOT overlap.)
    public boolean overlaps(TimeSlot other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    public boolean isSameDay() {
        return start.toLocalDate().equals(end.toLocalDate());
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
