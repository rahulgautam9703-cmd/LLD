package user;

import shared.AppException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UserService {

    /*final here freezes the reference, not the contents. You can still users.put(...) / emailToId.remove(...) / seq.incrementAndGet() all day — final only stops the variable from being reassigned to a different object.*/
    private final Map<String, User> users = new HashMap<>();        // id  -> User (primary store)
    private final Map<String, String> emailToId = new HashMap<>();   // email -> id (unique-constraint + lookup index) for quick lookup findByEmail
    private final AtomicInteger seq = new AtomicInteger(0); //private int seq = 0 atomic id generator; use UUID in production

    /*What should we return from register method (User or User ID)
    * Id: slow to query
    * User: Already immutable so safe, Light
    * */
    public String register(String name, String email, String phone) {
        if (emailToId.containsKey(email)) {   // O(1) uniqueness check via index (was O(n) stream scan)
            throw new AppException("User already registered: " + email, "USER_ALREADY_EXISTS");
        }
        String id = "U" + seq.incrementAndGet();
        User user = new User(id, name, email, phone);
        users.put(id, user);
        emailToId.put(email, id);
        return id;
    }

    public User findById(String id) {
        User user = users.get(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }
/*   While updating email id email-> id map might break
     User is immutable, so "update" = build a replacement with the same id + new email,
     then keep BOTH (email to map and useer id) maps in sync: move the emailToId entry off the old email onto the new one.*/
    public User updateEmail(String id, String newEmail) {
        User existing = findById(id);                 // throws UserNotFoundException if id is unknown

        if (existing.getEmail().equals(newEmail)) {   // no-op: nothing to change
            return existing;
        }

        // Reject only if the new email belongs to a DIFFERENT user (keeps email unique).
        String ownerOfNewEmail = emailToId.get(newEmail);
        if (ownerOfNewEmail != null && !ownerOfNewEmail.equals(id)) {
            throw new AppException("Email already in use: " + newEmail, "USER_ALREADY_EXISTS");
        }

        User updated = new User(id, existing.getName(), newEmail, existing.getPhone());
        users.put(id, updated);                       // 1. Replace same emailId
        emailToId.remove(existing.getEmail());        // 2a. drop the stale index entry...
        emailToId.put(newEmail, id);                  // 2b. ...and add the new one
        return updated;
    }
}

/*User service is responsible for ID generation not Client
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

* EMAIL id for uniqueness not primary key. To avoid different users with same EMAIL id. */
