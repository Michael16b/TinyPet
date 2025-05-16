package org.example;


import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.server.spi.auth.EspAuthenticator;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;


import com.google.appengine.api.datastore.*;

@Api(
    name = "petitionApi",
    version = "v1",
    namespace = @ApiNamespace(
        ownerDomain = "example.org",
        ownerName = "example.org",
        packagePath = ""
    )
)

public class PetitionEndpoint {

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private Entity checkAuth(String accessToken) throws UnauthorizedException {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new UnauthorizedException("Missing access token");
        }
        try {
            Key userKey = KeyFactory.createKey("User", accessToken);
            return datastore.get(userKey);
        } catch (EntityNotFoundException e) {
            throw new UnauthorizedException("Invalid access token");
        }
    }

    @ApiMethod(name = "createPetition", httpMethod = "post", path = "create")
    public Entity createPetition(
        @Named("title") String title,
        @Named("content") String content,
        @Named("tags") List<String> tags,
        @Named("access_token") String token
    ) throws UnauthorizedException {

        Entity user = checkAuth(token);

        Entity petition = new Entity("Petition");
        petition.setProperty("title", title);
        petition.setProperty("content", content);
        petition.setProperty("tags", tags);
        petition.setProperty("creatorEmail", user.getKey().getName());
        petition.setProperty("creationDate", new Date());
        petition.setProperty("signatureCount", 0L);

        datastore.put(petition);
        return petition;
    }

    @ApiMethod(name = "getAllPetitions", httpMethod = "get", path = "all")
    public List<Entity> getAllPetitions(@Named("access_token") String token) throws UnauthorizedException {
        checkAuth(token);

        Query query = new Query("Petition")
                .addSort("creationDate", Query.SortDirection.DESCENDING);
        PreparedQuery results = datastore.prepare(query);
        return results.asList(FetchOptions.Builder.withLimit(100));
    }

    @ApiMethod(name = "signPetition", httpMethod = "post", path = "sign")
    public String signPetition(
        @Named("petitionId") Long petitionId,
        @Named("access_token") String token
    ) throws UnauthorizedException {
        Entity user = checkAuth(token);
        String userEmail = user.getKey().getName();

        // Vérifier s'il a déjà signé
        Query checkQuery = new Query("Signature")
            .setFilter(CompositeFilterOperator.and(
                new FilterPredicate("petitionId", FilterOperator.EQUAL, petitionId),
                new FilterPredicate("userEmail", FilterOperator.EQUAL, userEmail)
            ));

        if (datastore.prepare(checkQuery).countEntities(FetchOptions.Builder.withDefaults()) > 0) {
            return "Already signed.";
        }

        // Enregistrer signature
        Entity signature = new Entity("Signature");
        signature.setProperty("petitionId", petitionId);
        signature.setProperty("userEmail", userEmail);
        signature.setProperty("signatureDate", new Date());
        datastore.put(signature);

        // Incrémenter signatureCount
        try {
            Key petitionKey = KeyFactory.createKey("Petition", petitionId);
            Entity petition = datastore.get(petitionKey);
            Long count = (Long) petition.getProperty("signatureCount");
            petition.setProperty("signatureCount", count + 1);
            datastore.put(petition);
        } catch (EntityNotFoundException e) {
            return "Petition not found.";
        }

        return "Signed successfully.";
    }

    @ApiMethod(name = "getSigners", httpMethod = "get", path = "signed")
    public List<Entity> getSigners(
        @Named("petitionId") Long petitionId,
        @Named("access_token") String token
    ) throws UnauthorizedException {
        checkAuth(token);

        Query query = new Query("Signature")
            .setFilter(new FilterPredicate("petitionId", FilterOperator.EQUAL, petitionId))
            .addSort("signatureDate", Query.SortDirection.DESCENDING);

        PreparedQuery pq = datastore.prepare(query);
        return pq.asList(FetchOptions.Builder.withLimit(100));
    }
}
