package org.example;

import com.google.appengine.api.datastore.Entity;

import java.util.ArrayList;
import java.util.List;

public class PetitionUtils {
    public static List<EmbeddedPetition> getEmbeddedPetitions(List<Entity> entities) {
        List<EmbeddedPetition> result = new ArrayList<>();
        for (Entity entity : entities) {
            result.add(new EmbeddedPetition(entity));
        }
        return result;
    }
}
