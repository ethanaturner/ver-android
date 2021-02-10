package turnerapps.vertv;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ethanturner on 2/11/17.
 */

public class VerUser {
    public String firstname;
    public String lastname;
    public String capacity;

    public VerUser() {
    }

    public VerUser(String firstname, String lastname, String capacity) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.capacity = capacity;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getCapacity() {
        return capacity;
    }

}