package org.example;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Api(name = "petitionApi", version = "v1") // name et version sont importants !
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

    @ApiMethod(name = "create", httpMethod = "post", path = "create")
    public PetitionResponse create(
            @Named("title") String title,
            @Named("content") String content,
            @Named("tags") List<String> tags,
            @Named("access_token") String token
    ) throws Exception {

        GoogleIdToken.Payload payload = verifyToken(token);
        String userEmail = payload.getEmail(); // Peut être utilisé plus tard pour lier au créateur

        Entity petition = new Entity("Petition");
        petition.setProperty("title", title);
        petition.setProperty("content", content);
        petition.setProperty("tags", tags);
        petition.setProperty("creatorEmail", userEmail);
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
    public List<Entity> list(@Named("access_token") String token) throws Exception {
        verifyToken(token); // On valide le token ici aussi
        Query query = new Query("Petition").addSort("creationDate", Query.SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(query);
        return pq.asList(FetchOptions.Builder.withLimit(100));
    }
}