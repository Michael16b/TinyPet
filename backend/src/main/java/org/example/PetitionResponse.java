package org.example;

import java.util.List;
import com.google.appengine.api.datastore.Entity;

public class PetitionResponse {
    public String status;
    public String message;
    public Long petitionId;
    private List<Entity> entities;
    private String nextCursor;

    public PetitionResponse() {}

    public PetitionResponse(String status, String message, Long petitionId) {
        this.status = status;
        this.message = message;
        this.petitionId = petitionId;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }
}
