package user;

import shared.AppException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

// In-memory user store (Repository pattern). Source of truth for User lifecycle AND identity.
// Keyed by the surrogate id -> O(1) lookup, and id is immutable so it's a safe key (email isn't).
public class UserService {

    private final Map<String, User> users = new HashMap<>();
    private final AtomicInteger seq = new AtomicInteger(0); // atomic id generator; use UUID in production

    // Service owns id generation: caller passes data, gets back a User with an assigned id.
    public User register(String name, String email, String phone) {
        // email is still a business-unique field even though it's not the key
        boolean emailTaken = users.values().stream().anyMatch(u -> u.getEmail().equals(email));
        if (emailTaken) {
            throw new AppException("User already registered: " + email, "USER_ALREADY_EXISTS");
        }
        String id = "U" + seq.incrementAndGet();
        User user = new User(id, name, email, phone);
        users.put(id, user);
        return user;
    }

    public User findById(String id) {
        User user = users.get(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }
}

/*User service is reponsible for ID generation not Client
* User is immutable
* Production would use UUID.randomUUID() or a DB sequence
* int seq = 0; can cause race condition can result in same id in multi threaded env
*
* How AtomicInteger fixes it
++seq looks like one step but is actually three: read seq → add 1 → write back. With two threads, both can read the same value before either writes:
seq = 5
Thread A: reads 5 ─┐
Thread B: reads 5 ─┤   (both read before either writes)
Thread A: writes 6 │
Thread B: writes 6 ┘   → both users get id "U6"  ✗  (and seq is 6, not 7)
AtomicInteger.incrementAndGet() makes read-add-write a single indivisible (atomic) hardware operation (CPU compare-and-swap). No other thread can interleave mid-way, so every caller gets a distinct value:
private final AtomicInteger seq = new AtomicInteger(0);
...
String id = "U" + seq.incrementAndGet();   // A gets U6, B gets U7 — guaranteed unique
So: int ++ = 3 racy steps → duplicate ids; AtomicInteger = 1 atomic step → always unique. (UUID.randomUUID() sidesteps the shared counter entirely — no coordination needed.)
* */
