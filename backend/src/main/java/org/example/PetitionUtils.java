package org.example;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Key;

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

    public static Query.Filter applyFilters(
            List<Key> petitionKeys,
            String tag,
            String userEmail,
            String userSearch,
            String userSearchField
    ) {
        List<Query.Filter> filters = new ArrayList<>();
        // Filtre IN sur la clé (si fourni)
        if (petitionKeys != null && !petitionKeys.isEmpty()) {
            filters.add(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.IN, petitionKeys));
        }
        if (tag != null && !tag.trim().isEmpty()) {
            filters.add(new Query.FilterPredicate("tags", Query.FilterOperator.EQUAL, tag));
        }
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            filters.add(new Query.FilterPredicate("creatorEmail", Query.FilterOperator.EQUAL, userEmail));
        }
        if (userSearch != null && !userSearch.trim().isEmpty()) {
            String field = switch (userSearchField) {
                case "creatorFirstName" -> "creatorFirstName";
                case "creatorLastName" -> "creatorLastName";
                default -> "creatorEmail";
            };
            filters.add(new Query.FilterPredicate(field, Query.FilterOperator.EQUAL, userSearch));
        }

        if (filters.isEmpty()) {
            return null;
        } else if (filters.size() == 1) {
            return filters.get(0);
        } else {
            return new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filters);
        }
    }
}
