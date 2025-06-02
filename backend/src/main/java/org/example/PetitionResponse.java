package org.example;

public class PetitionResponse {
    public String status;
    public String message;
    public Long petitionId;

    public PetitionResponse() {}

    public PetitionResponse(String status, String message, Long petitionId) {
        this.status = status;
        this.message = message;
        this.petitionId = petitionId;
    }
}
