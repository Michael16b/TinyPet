package org.example;

import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.*;

import java.util.Collections;
import java.util.logging.Logger;

import com.google.api.server.spi.config.Api;

import static org.example.PetitionUtils.getEmbeddedPetitions;


@Api(
  name = "petitionApi",
  version = "v1",
  clientIds = {
    "598050199229-8svis83vs9bug6d5tpjqjta3jnbusdan.apps.googleusercontent.com"
  },
  audiences = "598050199229-8svis83vs9bug6d5tpjqjta3jnbusdan.apps.googleusercontent.com",
  namespace = 
    @ApiNamespace(
    ownerDomain = "tinypet-atalla-besily-jan.ew.r.appspot.com",
    ownerName = "tinypet-atalla-besily-jan.ew.r.appspot.com",
    packagePath = ""
  )
)
public class PetitionEndpoint {

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static final Logger logger = Logger.getLogger(PetitionEndpoint.class.getName());

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
                throw new UnauthorizedException("Token de connexion Invalide");
            }
        } catch (Exception e) {
            throw new UnauthorizedException("Utilisateur non connecté");
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
        @Nullable @Named("sortBy") String sortBy,
        @Nullable @Named("sortOrder") String sortOrder,
        @Nullable @Named("tag") String tag,
        @Nullable @Named("signedByUserEmail") String signedByUserEmail,
        @Nullable @Named("userEmail") String userEmail,
        @Nullable @Named("userSearch") String userSearch,
        @Nullable @Named("userSearchField") String userSearchField,
        @Nullable @Named("cursor") String cursor
) throws Exception {

    verifyToken(token);
    int pageSize = (limit != null) ? limit : 20;

    logger.info("Listing petitions with parameters: " +
            "limit=" + pageSize +
            ", sortBy=" + sortBy +
            ", sortOrder=" + sortOrder +
            ", tag=" + tag +
            ", signedByUserEmail=" + signedByUserEmail +
            ", userEmail=" + userEmail +
            ", userSearch=" + userSearch +
            ", userSearchField=" + userSearchField +
            ", cursor=" + cursor);

    if (signedByUserEmail != null && !signedByUserEmail.trim().isEmpty()) {
        List<Key> petitionKeys = getPetitionKeysSignedByUser(signedByUserEmail);

        Query petitionQuery = new Query("Petition");
        Query.Filter filter = PetitionUtils.applyFilters(petitionKeys, tag, userEmail, userSearch, userSearchField);
        if (filter != null) petitionQuery.setFilter(filter);

        List<Entity> petitions = new ArrayList<>(datastore.prepare(petitionQuery).asList(FetchOptions.Builder.withDefaults()));
        sortEntities(petitions, sortBy, sortOrder);

        PetitionResponse response = new PetitionResponse();
        response.setEntities(getEmbeddedPetitions(petitions));
        response.setNextCursor(null);
        return response;
    }

    Query query = new Query("Petition");
    applyFilters(query, tag, userEmail, userSearch, userSearchField);
    applySort(query, sortBy, sortOrder);

    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);
    if (cursor != null && !cursor.isEmpty()) {
        fetchOptions.startCursor(Cursor.fromWebSafeString(cursor));
    }

    QueryResultList<Entity> entities = datastore.prepare(query).asQueryResultList(fetchOptions);
    PetitionResponse response = new PetitionResponse();
    response.setEntities(getEmbeddedPetitions(entities));
    response.setNextCursor(entities.getCursor() != null ? entities.getCursor().toWebSafeString() : null);

    return response;
}
private List<Key> getPetitionKeysSignedByUser(String email) throws NotFoundException {
    Query signatureQuery = new Query("Signature")
        .setFilter(new Query.FilterPredicate("userEmail", Query.FilterOperator.EQUAL, email));

    List<Entity> signatures = datastore.prepare(signatureQuery)
        .asList(FetchOptions.Builder.withLimit(30));

    List<Long> petitionIds = signatures.stream()
        .map(sig -> (Long) sig.getProperty("petitionId"))
        .filter(Objects::nonNull)
        .toList();

    if (petitionIds.isEmpty()) {
        logger.info("No signatures found for user: " + email);
        throw new NotFoundException("Aucune pétition signée trouvée pour cet utilisateur.");
    }

    return petitionIds.stream()
        .map(id -> KeyFactory.createKey("Petition", id))
        .toList();
}
private void applyFilters(Query query, String tag, String userEmail, String userSearch, String userSearchField) {
    List<Query.Filter> filters = new ArrayList<>();

    PetitionUtils.filterDetails(tag, userEmail, userSearch, userSearchField, filters);

    if (!filters.isEmpty()) {
        query.setFilter(filters.size() == 1 ? filters.get(0) : Query.CompositeFilterOperator.and(filters));
    }
}



    private void applySort(Query query, String sortBy, String sortOrder) {
    String sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "creationDate";
    Query.SortDirection direction = "asc".equalsIgnoreCase(sortOrder)
            ? Query.SortDirection.ASCENDING
            : Query.SortDirection.DESCENDING;

    query.addSort(sortField, direction);

    if (!"creationDate".equals(sortField)) {
        query.addSort("creationDate", Query.SortDirection.DESCENDING);
    }
}
private void sortEntities(List<Entity> entities, String sortBy, String sortOrder) {
    if (sortBy == null || sortBy.isEmpty()) return;

    entities.sort((e1, e2) -> {
        Object val1 = e1.getProperty(sortBy);
        Object val2 = e2.getProperty(sortBy);

        if (val1 == null && val2 == null) return 0;
        if (val1 == null) return 1;
        if (val2 == null) return -1;

        if (val1 instanceof Number && val2 instanceof Number) {
            return "desc".equalsIgnoreCase(sortOrder)
                    ? Double.compare(((Number) val2).doubleValue(), ((Number) val1).doubleValue())
                    : Double.compare(((Number) val1).doubleValue(), ((Number) val2).doubleValue());
        }

        if (val1 instanceof Comparable && val2 instanceof Comparable) {
            @SuppressWarnings("unchecked")
            int cmp = ((Comparable<Object>) val1).compareTo(val2);
            return "desc".equalsIgnoreCase(sortOrder) ? -cmp : cmp;
        }

        return 0;
    });
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

            Query query = new Query("Signature")
                    .setFilter(Query.CompositeFilterOperator.and(
                            new Query.FilterPredicate("petitionId", Query.FilterOperator.EQUAL, petitionId),
                            new Query.FilterPredicate("userEmail", Query.FilterOperator.EQUAL, userEmail)
                    ));
            if (!datastore.prepare(query).asList(FetchOptions.Builder.withDefaults()).isEmpty()) {
                throw new ConflictException("Vous avez déjà signé cette pétition.");
            }

            Entity signature = new Entity("Signature");
            signature.setProperty("petitionId", petitionId);
            signature.setProperty("userEmail", userEmail);
            signature.setProperty("signedAt", new Date());
            signature.setProperty("firstName", firstName);
            signature.setProperty("lastName", lastName);
            datastore.put(signature);

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


}