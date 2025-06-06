package org.example;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
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
import com.google.api.server.spi.config.ApiNamespace;


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
    String userId = payload.getSubject(); // identifiant unique Google
    Key userKey = KeyFactory.createKey("User", userId);
    
    try {
        // Essayer de récupérer l'utilisateur
        datastore.get(userKey);
    } catch (EntityNotFoundException e) {
        // Si pas trouvé, créer un nouvel utilisateur
        Entity user = new Entity("User", userId);
        user.setProperty("email", payload.getEmail());
        user.setProperty("name", (String) payload.get("given_name"));
        user.setProperty("family_name", (String) payload.get("family_name"));
        user.setProperty("picture", (String) payload.get("picture"));
        user.setProperty("createdAt", new Date());
        datastore.put(user);
    }
}


    @ApiMethod(name = "create", httpMethod = "post", path = "create")
    public PetitionResponse create(
            @Named("title") String title,
            @Named("content") String content,
            @Named("tags") List<String> tags,
            @Named("access_token") String token
    ) throws Exception {

        GoogleIdToken.Payload payload = verifyToken(token);
        ensureUserExists(payload);
        String userEmail = payload.getEmail(); // Peut être utilisé plus tard pour lier au créateur
        String userId = payload.getSubject(); // ID Google unique de l'utilisateur

        Entity petition = new Entity("Petition");
        petition.setProperty("title", title);
        petition.setProperty("content", content);
        petition.setProperty("tags", tags);
        petition.setProperty("creatorEmail", userEmail);
        petition.setProperty("creatorId", userId);
        petition.setProperty("creationDate", new Date());
        petition.setProperty("signatureCount", 0L);

        datastore.put(petition);
        logger.info("La création de la pétition a réussi !");
        return new PetitionResponse(
                "success",
                "Petition created successfully",
                petition.getKey().getId()
        );
    }

    @ApiMethod(name = "list", httpMethod = "get", path = "list")
public List<EmbeddedPetition> list(@Named("access_token") String token) throws Exception {
    verifyToken(token);
    Query query = new Query("Petition").addSort("creationDate", Query.SortDirection.DESCENDING);
    List<Entity> petitionEntities = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
    List<EmbeddedPetition> result = new ArrayList<>();

    for (Entity petition : petitionEntities) {
        String creatorId = (String) petition.getProperty("creatorId");
        Entity user = datastore.get(KeyFactory.createKey("User", creatorId));
        result.add(new EmbeddedPetition(petition, user));
    }

    return result;
}


    @ApiMethod(name = "sign", httpMethod = "post", path = "sign")
public PetitionResponse sign(
        @Named("petitionId") long petitionId,
        @Named("access_token") String token
) throws Exception {
    GoogleIdToken.Payload payload = verifyToken(token);
    ensureUserExists(payload);
    String userEmail = payload.getEmail();

    // Vérifie si la personne a déjà signé
    Key petitionKey = KeyFactory.createKey("Petition", petitionId);
    Query query = new Query("Signature")
            .setFilter(Query.CompositeFilterOperator.and(
                    new Query.FilterPredicate("petitionId", Query.FilterOperator.EQUAL, petitionId),
                    new Query.FilterPredicate("userEmail", Query.FilterOperator.EQUAL, userEmail)
            ));
    if (!datastore.prepare(query).asList(FetchOptions.Builder.withDefaults()).isEmpty()) {
        return new PetitionResponse("already_signed", "You have already signed this petition.", petitionId);
    }

    // Enregistre la signature
    Entity signature = new Entity("Signature");
    signature.setProperty("petitionId", petitionId);
    signature.setProperty("userEmail", userEmail);
    signature.setProperty("signedAt", new Date());
    datastore.put(signature);

    // Incrémente le compteur
    Transaction txn = datastore.beginTransaction();
    try {
        Entity petition = datastore.get(petitionKey);
        long count = (Long) petition.getProperty("signatureCount");
        petition.setProperty("signatureCount", count + 1);
        datastore.put(petition);
        txn.commit();
    } finally {
        if (txn.isActive()) txn.rollback();
    }

    return new PetitionResponse("success", "Petition signed successfully.", petitionId);
}
@ApiMethod(name = "signedPetitions", httpMethod = "get", path = "signed")
public List<Entity> signedPetitions(@Named("access_token") String token) throws Exception {
    GoogleIdToken.Payload payload = verifyToken(token);
    String userEmail = payload.getEmail();

    // Récupère toutes les signatures de l'utilisateur
    Query signatureQuery = new Query("Signature")
            .setFilter(new Query.FilterPredicate("userEmail", Query.FilterOperator.EQUAL, userEmail))
            .addSort("signedAt", Query.SortDirection.DESCENDING);

    List<Entity> signatures = datastore.prepare(signatureQuery).asList(FetchOptions.Builder.withLimit(100));
    List<Entity> petitions = new java.util.ArrayList<>();

    for (Entity sig : signatures) {
        long petitionId = (Long) sig.getProperty("petitionId");
        Key petitionKey = KeyFactory.createKey("Petition", petitionId);
        try {
            petitions.add(datastore.get(petitionKey));
        } catch (EntityNotFoundException e) {
            // Ignore missing petitions
        }
    }
    return petitions;
}
@ApiMethod(name = "popularPetitions", httpMethod = "get", path = "popular")
public List<Entity> popularPetitions(@Named("access_token") String token) throws Exception {
    verifyToken(token);
    Query query = new Query("Petition")
            .addSort("signatureCount", Query.SortDirection.DESCENDING)
            .addSort("creationDate", Query.SortDirection.DESCENDING);

    return datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
}
@ApiMethod(name = "searchByTag", httpMethod = "get", path = "search")
public List<Entity> searchByTag(
        @Named("tag") String tag,
        @Named("access_token") String token
) throws Exception {
    verifyToken(token);
    Query.Filter tagFilter = new Query.FilterPredicate("tags", Query.FilterOperator.EQUAL, tag);
    Query query = new Query("Petition")
            .setFilter(tagFilter)
            .addSort("creationDate", Query.SortDirection.DESCENDING);

    return datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
}
@ApiMethod(name = "listSigners", httpMethod = "get", path = "signers")
public List<Entity> listSigners(
        @Named("petitionId") long petitionId,
        @Named("access_token") String token
) throws Exception {
    verifyToken(token);
    Query query = new Query("Signature")
            .setFilter(new Query.FilterPredicate("petitionId", Query.FilterOperator.EQUAL, petitionId))
            .addSort("signedAt", Query.SortDirection.DESCENDING);

    return datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
}

}