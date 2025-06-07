package org.example;

import java.util.List;
import com.google.appengine.api.datastore.Entity;

public class PetitionResponse {
    private String status;
    private String message;
    private Long petitionId;
    private List<EmbeddedPetition> entities;
    private String nextCursor;

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

    public List<EmbeddedPetition> getEntities() {
        return entities;
    }

    public void setEntities(List<EmbeddedPetition> entities) {
        this.entities = entities;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }
}

