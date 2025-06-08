package org.example;

import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.*;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.Collections;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

        Query query = new Query("Petition");

        if (tag != null && !tag.trim().isEmpty()) {
            query.setFilter(new Query.FilterPredicate("tags", Query.FilterOperator.EQUAL, tag));
        }

        String sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "creationDate";
        Query.SortDirection direction =
                (sortOrder != null && sortOrder.equalsIgnoreCase("asc")) ?
                        Query.SortDirection.ASCENDING : Query.SortDirection.DESCENDING;
        query.addSort(sortField, direction);

        if (!"creationDate".equals(sortField)) {
            query.addSort("creationDate", Query.SortDirection.DESCENDING);
        }
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            query.setFilter(new Query.FilterPredicate("creatorEmail", Query.FilterOperator.EQUAL, userEmail));
        }

        if (signedByUserEmail != null && !signedByUserEmail.trim().isEmpty()) {
            // 1. Récupérer les IDs de pétitions signées (max 30)
            Query signatureQuery = new Query("Signature")
                    .setFilter(new Query.FilterPredicate("userEmail", Query.FilterOperator.EQUAL, signedByUserEmail));
            List<Entity> userSignatures = datastore.prepare(signatureQuery)
                    .asList(FetchOptions.Builder.withLimit(30)); // Limite de 30 signatures

            List<Long> petitionIds = userSignatures.stream()
                    .map(sig -> (Long) sig.getProperty("petitionId"))
                    .filter(Objects::nonNull)
                    .toList();

            if (petitionIds.isEmpty()) {
                logger.info("No signatures found for user: " + signedByUserEmail);
                throw new NotFoundException("Aucune pétition signée trouvée pour cet utilisateur.");
            }

            // 2. Construire la liste des clés à partir des IDs
            List<Key> petitionKeys = petitionIds.stream()
                    .map(id -> KeyFactory.createKey("Petition", id))
                    .toList();

            // 3. Requête sur Petition avec filtre IN sur la clé (max 30)
            Query petitionQuery = new Query("Petition")
                    .setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.IN, petitionKeys));

            // 4. Appliquer les autres filtres si besoin (tag, creator, etc.) en mémoire ou dans la requête si possible

            // 5. Faire le tri (en mémoire si besoin)
            List<Entity> petitions = new ArrayList<>(datastore.prepare(petitionQuery).asList(FetchOptions.Builder.withDefaults()));
            // Tri en mémoire si nécessaire
            if (sortBy != null && !sortBy.isEmpty()) {
                petitions.sort((e1, e2) -> {
                    Object val1 = e1.getProperty(sortBy);
                    Object val2 = e2.getProperty(sortBy);

                    // Pour des propriétés numériques (Long, Integer, Double, etc.)
                    if (val1 instanceof Number && val2 instanceof Number) {
                        double d1 = ((Number) val1).doubleValue();
                        double d2 = ((Number) val2).doubleValue();
                        return "desc".equalsIgnoreCase(sortOrder) ? Double.compare(d2, d1) : Double.compare(d1, d2);
                    }

                    // Pour des propriétés String ou autres types Comparable
                    if (val1 instanceof Comparable && val2 instanceof Comparable) {
                        @SuppressWarnings("unchecked")
                        int cmp = ((Comparable<Object>) val1).compareTo(val2);
                        return "desc".equalsIgnoreCase(sortOrder) ? -cmp : cmp;
                    }

                    // nulls last
                    if (val1 == null && val2 == null) return 0;
                    if (val1 == null) return 1;
                    if (val2 == null) return -1;
                    return 0;
                });
            }

            // 6. Mapping & réponse (ignorer la limite paramètre, retourner tout ce qu’on a)
            List<EmbeddedPetition> embeddedPetitions = getEmbeddedPetitions(petitions);

            PetitionResponse response = new PetitionResponse();
            response.setEntities(embeddedPetitions);
            response.setNextCursor(null); // Pas de cursor car tout est renvoyé en une fois
            return response;
        }

        if (userSearch != null && !userSearch.trim().isEmpty()) {
            if (userSearchField != null && userSearchField.equals("creatorFirstName")) {
                logger.info("Filtering by creatorFirstName: " + userSearch);
                query.setFilter(new Query.FilterPredicate("creatorFirstName", Query.FilterOperator.EQUAL, userSearch));
            } else if (userSearchField != null && userSearchField.equals("creatorLastName")) {
                logger.info("Filtering by creatorLastName: " + userSearch);
                query.setFilter(new Query.FilterPredicate("creatorLastName", Query.FilterOperator.EQUAL, userSearch));
            } else  {
                logger.info("Filtering by creatorEmail: " + userSearch);
                query.setFilter(new Query.FilterPredicate("creatorEmail", Query.FilterOperator.EQUAL, userSearch));
            }
        }

        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);
        if (cursor != null && !cursor.isEmpty()) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(cursor));
        }

        PreparedQuery pq = datastore.prepare(query);
        logger.info(": " + pq.toString());
        QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);
        List<EmbeddedPetition> embeddedPetitions = getEmbeddedPetitions(entities);
        Cursor nextCursor = entities.getCursor();

        PetitionResponse response = new PetitionResponse();
        response.setEntities(embeddedPetitions);
        response.setNextCursor(nextCursor != null ? nextCursor.toWebSafeString() : null);

        return response;
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


}