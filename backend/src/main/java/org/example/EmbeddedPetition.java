package org.example;
import java.util.List;
import com.google.appengine.api.datastore.Entity;



public class EmbeddedPetition {
    public String title;
    public String content;
    public List<String> tags;
    public long signatureCount;
    public long id;
    public String creatorName;
    public String creatorFamilyName;
    public String creatorPicture;

    public EmbeddedPetition(Entity petition, Entity user) {
        this.title = (String) petition.getProperty("title");
        this.content = (String) petition.getProperty("content");
        this.tags = (List<String>) petition.getProperty("tags");
        this.signatureCount = (Long) petition.getProperty("signatureCount"); // 👈 ici
        this.id = petition.getKey().getId();
        this.creatorName = (String) user.getProperty("name");
        this.creatorFamilyName = (String) user.getProperty("family_name");
        this.creatorPicture = (String) user.getProperty("picture");
    }
}

