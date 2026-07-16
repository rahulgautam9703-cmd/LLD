package user;

public class User {
    private final String id;          // surrogate key: immutable, assigned by UserService
    private final String name;
    private final String email;
    private final String phone;

    // Package-private: only UserService (same package) creates users, so it owns id assignment.
    User(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}

/*
*Participant has-a User (composition/reference), it does not extend it. A participant wraps a user and adds meeting-specific context:
* Another pro is while sending notification Participant can send User instead of this while calling say email notification
* Pros
* USer exists independently outside meeting also
* User can be participant, author, scrum master etc with Participant having context just related to booking.
* Basically Entity VS role
* User is reusable everywhere and it is not an observer like participant is in case of meeting event
*Remember Participant is observer USer is not
*Cons
* Over engineering - for our LLD user doesnt have any extra use
Why composition and not inheritance OR why Participant holds a User rather than extends User:
*
* */
