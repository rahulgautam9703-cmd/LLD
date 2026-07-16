package user;

import shared.AppException;

import java.util.HashMap;
import java.util.Map;

// In-memory user store (Repository pattern). Source of truth for User lifecycle AND identity.
// Keyed by the surrogate id -> O(1) lookup, and id is immutable so it's a safe key (email isn't).
public class UserService {

    private final Map<String, User> users = new HashMap<>();
    private int seq = 0; // simple in-memory id generator; use UUID in production

    // Service owns id generation: caller passes data, gets back a User with an assigned id.
    public User register(String name, String email, String phone) {
        // email is still a business-unique field even though it's not the key
        boolean emailTaken = users.values().stream().anyMatch(u -> u.getEmail().equals(email));
        if (emailTaken) {
            throw new AppException("User already registered: " + email, "USER_ALREADY_EXISTS");
        }
        String id = "U" + (++seq);
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
* */
