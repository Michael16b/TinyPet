package org.example;

import com.google.appengine.api.datastore.Entity;
import java.util.Date;

public class SignerInfo {
    public String firstName;
    public String lastName;
    public String email;
    public Date signedAt;

    public SignerInfo(Entity signature) {
        this.firstName = (String) signature.getProperty("firstName");
        this.lastName = (String) signature.getProperty("lastName");
        this.email = (String) signature.getProperty("userEmail");
        this.signedAt = (Date) signature.getProperty("signedAt");
    }
}


