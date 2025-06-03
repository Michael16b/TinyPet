package org.example;


import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.*;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Api(name = "petitionApi", version = "v1") // name et version sont importants !
public class PetitionEndpoint {

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static final Logger logger = Logger.getLogger(PetitionEndpoint.class.getName());
    private Entity checkAuth(String accessToken) throws Exception {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new Exception("Missing access token");
        }
        try {
            Key userKey = KeyFactory.createKey("User", accessToken);
            return datastore.get(userKey);
        } catch (EntityNotFoundException e) {
            throw new Exception("Invalid access token");
        }
    }

    @ApiMethod(name = "create", httpMethod = "post", path = "create")
    public PetitionResponse create(
            @Named("title") String title,
            @Named("content") String content,
            @Named("tags") List<String> tags,
            @Named("access_token") String token
    ) throws Exception {

        //Entity user = checkAuth(token);

        Entity petition = new Entity("Petition");
        petition.setProperty("title", title);
        petition.setProperty("content", content);
        petition.setProperty("tags", tags);
        //petition.setProperty("creatorEmail", user.getKey().getName());
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
        //checkAuth(token);
        Query query = new Query("Petition").addSort("creationDate", Query.SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(query);
        return pq.asList(FetchOptions.Builder.withLimit(100));
    }
}
