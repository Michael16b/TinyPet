package org.example;

import java.util.List;
import com.google.appengine.api.datastore.Entity;

public class PetitionResponse {
    private String status;
    private String message;
    private Long petitionId;

    public PetitionResponse() {}

    public PetitionResponse(String status, String message, Long petitionId) {
        this.status = status;
        this.message = message;
        this.petitionId = petitionId;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getPetitionId() { return petitionId; }
    public void setPetitionId(Long petitionId) { this.petitionId = petitionId; }
}

