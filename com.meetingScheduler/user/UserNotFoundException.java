package user;

import shared.AppException;

public class UserNotFoundException extends AppException {
    public UserNotFoundException(String email) {
        super("User not found: " + email, "USER_NOT_FOUND");
    }
}
