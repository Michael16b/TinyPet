package org.example;

import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.util.ArrayList;


import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import com.google.api.server.spi.config.Api;


@Api(
  name = "petitionApi",
  version = "v1",
  clientIds = {
    "598050199229-8svis83vs9bug6d5tpjqjta3jnbusdan.apps.googleusercontent.com"
  },
  audiences = "598050199229-8svis83vs9bug6d5tpjqjta3jnbusdan.apps.googleusercontent.com",
  namespace = 
    @ApiNamespace(
    ownerDomain = "example.com",
    ownerName = "example.com",
    packagePath = ""
  )
)
public class PetitionEndpoint {

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static final Logger logger = Logger.getLogger(PetitionEndpoint.class.getName());

    // Ton CLIENT_ID Google (remplace-le par le tien si besoin)
    private static final String CLIENT_ID = "598050199229-8svis83vs9bug6d5tpjqjta3jnbusdan.apps.googleusercontent.com";
    private static final int NUM_SHARDS = 10;

    private GoogleIdToken.Payload verifyToken(String idTokenString) throws UnauthorizedException {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            } else {
                throw new UnauthorizedException("Invalid ID token.");
            }
        } catch (Exception e) {
            throw new UnauthorizedException("Token verification failed: " + e.getMessage());
    }
}

private void ensureUserExists(GoogleIdToken.Payload payload) {
    String userId = payload.getSubject();
    Key userKey = KeyFactory.createKey("User", userId);
    
    try {
        datastore.get(userKey);
    } catch (EntityNotFoundException e) {
        Entity user = new Entity("User", userId);
        user.setProperty("email", payload.getEmail());
        user.setProperty("name", (String) payload.get("given_name"));
        user.setProperty("family_name", (String) payload.get("family_name"));
        user.setProperty("picture", (String) payload.get("picture"));
        user.setProperty("createdAt", new Date());
        datastore.put(user);
    }
}

private void initializeCounterShards(long petitionId) {
    for (int i = 0; i < NUM_SHARDS; i++) {
        String shardId = petitionId + "-shard-" + i;
        Entity shard = new Entity("SignatureCounterShard", shardId);
        shard.setProperty("petitionId", petitionId);
        shard.setProperty("count", 0L);
        datastore.put(shard);
    }
}

private long getSignatureCount(long petitionId) {
    Query query = new Query("SignatureCounterShard")
            .setFilter(new Query.FilterPredicate("petitionId", Query.FilterOperator.EQUAL, petitionId));
    List<Entity> shards = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    long total = 0;
    for (Entity shard : shards) {
        total += (Long) shard.getProperty("count");
    }
    return total;
}



@ApiMethod(name = "create", httpMethod = "post", path = "create")
public PetitionResponse create(
        @Named("title") String title,
        @Named("content") String content,
        @Named("tags") List<String> tags,
        @Named("access_token") String token) throws Exception {

        GoogleIdToken.Payload payload = verifyToken(token);
        ensureUserExists(payload);

        String userEmail = payload.getEmail();
        String userId = payload.getSubject();

        String firstName = payload.get("given_name") != null ? payload.get("given_name").toString() : "";
        String lastName = payload.get("family_name") != null ? payload.get("family_name").toString() : "";

        Entity petition = new Entity("Petition");
        petition.setProperty("title", title);
        petition.setProperty("content", content);
        petition.setProperty("tags", tags);
        petition.setProperty("creatorEmail", userEmail);
        petition.setProperty("creatorId", userId);
        petition.setProperty("creatorFirstName", firstName);
        petition.setProperty("creatorLastName", lastName);
        petition.setProperty("creationDate", new Date());
        petition.setProperty("signatureCount", 0L);

        datastore.put(petition);
        initializeCounterShards(petition.getKey().getId());

        logger.info("Création pétition réussie par " + firstName + " " + lastName);

        return new PetitionResponse(
                "success",
                "Petition created successfully",
                petition.getKey().getId()
        );
}


    @ApiMethod(name = "list", httpMethod = "get", path = "list")
    public PetitionResponse list(
            @Named("access_token") String token,
            @Nullable @Named("limit") Integer limit,
            @Nullable @Named("cursor") String cursor) throws Exception {

        verifyToken(token);

        int pageSize = (limit != null) ? limit : 20;

        Query query = new Query("Petition").addSort("creationDate", Query.SortDirection.DESCENDING);
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);

        if (cursor != null && !cursor.isEmpty()) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(cursor));
        }

        PreparedQuery pq = datastore.prepare(query);
        QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);

        List<EmbeddedPetition> embeddedPetitions = getEmbeddedPetitions(entities);

        Cursor nextCursor = entities.getCursor();

        PetitionResponse response = new PetitionResponse();
        response.setEntities(embeddedPetitions);
        response.setNextCursor(nextCursor != null ? nextCursor.toWebSafeString() : null);

        return response;
    }

    private List<EmbeddedPetition> getEmbeddedPetitions(List<Entity> petitionEntities) {
        List<EmbeddedPetition> result = new ArrayList<>();
        for (Entity petition : petitionEntities) {
            result.add(new EmbeddedPetition(petition));
        }
        return result;
    }

    @ApiMethod(name = "sign", httpMethod = "post", path = "sign")
    public PetitionResponse sign(
            @Named("petitionId") long petitionId,
            @Named("access_token") String token) throws Exception, ConflictException {
            if (petitionId <= 0) {
                throw new IllegalArgumentException("Invalid petition ID.");
            }
            GoogleIdToken.Payload payload = verifyToken(token);
            ensureUserExists(payload);

            String userEmail = payload.getEmail();
            String firstName = payload.get("given_name") != null ? payload.get("given_name").toString() : "";
            String lastName = payload.get("family_name") != null ? payload.get("family_name").toString() : "";

            Key petitionKey = KeyFactory.createKey("Petition", petitionId);

            // Vérifie si la personne a déjà signé
            Query query = new Query("Signature")
                    .setFilter(Query.CompositeFilterOperator.and(
                            new Query.FilterPredicate("petitionId", Query.FilterOperator.EQUAL, petitionId),
                            new Query.FilterPredicate("userEmail", Query.FilterOperator.EQUAL, userEmail)
                    ));
            if (!datastore.prepare(query).asList(FetchOptions.Builder.withDefaults()).isEmpty()) {
                throw new ConflictException("Vous avez déjà signé cette pétition.");
            }

            // Enregistre la signature avec nom/prénom
            Entity signature = new Entity("Signature");
            signature.setProperty("petitionId", petitionId);
            signature.setProperty("userEmail", userEmail);
            signature.setProperty("signedAt", new Date());
            signature.setProperty("firstName", firstName);
            signature.setProperty("lastName", lastName);
            datastore.put(signature);

            // Incrémente le compteur de signatures
            int shardIndex = (int) (Math.random() * NUM_SHARDS);
            String shardId = petitionId + "-shard-" + shardIndex;

            Transaction txn = datastore.beginTransaction();
            try {
                Entity shard;
                try {
                    shard = datastore.get(txn, KeyFactory.createKey("SignatureCounterShard", shardId));
                } catch (EntityNotFoundException e) {
                    shard = new Entity("SignatureCounterShard", shardId);
                    shard.setProperty("petitionId", petitionId);
                    shard.setProperty("count", 0L);
                }
                long count = (Long) shard.getProperty("count");
                shard.setProperty("count", count + 1);
                datastore.put(txn, shard);
                txn.commit();
            } finally {
                if (txn.isActive()) txn.rollback();
            }
        Transaction countTxn = datastore.beginTransaction();
        try {
            Entity petition = datastore.get(countTxn, petitionKey);
            long currentCount = (Long) petition.getProperty("signatureCount");
            petition.setProperty("signatureCount", currentCount + 1);
            datastore.put(countTxn, petition);
            countTxn.commit();
        } finally {
            if (countTxn.isActive()) countTxn.rollback();
        }


            return new PetitionResponse("success", "Petition signed successfully.", petitionId);
    }

@ApiMethod(name = "getSigners", httpMethod = "get", path = "petition/{petitionId}/signers")
public SignersResponse getSigners(
        @Named("petitionId") long petitionId,
        @Named("access_token") String token) throws Exception {

        verifyToken(token);
        Query query = new Query("Signature")
                .setFilter(new Query.FilterPredicate("petitionId", Query.FilterOperator.EQUAL, petitionId))
                .addSort("signedAt", Query.SortDirection.DESCENDING);

        List<Entity> signatureEntities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        List<SignerInfo> signers = new ArrayList<>();

        for (Entity sig : signatureEntities) {
            signers.add(new SignerInfo(sig));
        }

        long totalSignatures = getSignatureCount(petitionId);


        return new SignersResponse(totalSignatures, signers);
}

@ApiMethod(name = "searchByTag", httpMethod = "get", path = "petition/searchByTag")
public List<EmbeddedPetition> searchByTag(
        @Named("tag") String tag,
        @Named("access_token") String token) throws Exception {
        verifyToken(token);

        Query query = new Query("Petition")
                .setFilter(new Query.FilterPredicate("tags", Query.FilterOperator.EQUAL, tag))
                .addSort("creationDate", Query.SortDirection.DESCENDING);

        List<Entity> petitionEntities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        List<EmbeddedPetition> result = new ArrayList<>();

        for (Entity petition : petitionEntities) {
            result.add(new EmbeddedPetition(petition));
        }

        return result;
}

@ApiMethod(name = "popular", httpMethod = "get", path = "petition/popular")
public List<EmbeddedPetition> getPopularPetitions(
        @Named("access_token") String token) throws Exception {
        verifyToken(token);

        Query query = new Query("Petition")
                .addSort("signatureCount", Query.SortDirection.DESCENDING)
                .addSort("creationDate", Query.SortDirection.DESCENDING);

        List<Entity> petitionEntities = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
        List<EmbeddedPetition> result = new ArrayList<>();

        for (Entity petition : petitionEntities) {
            result.add(new EmbeddedPetition(petition));
        }

        return result;
}


}