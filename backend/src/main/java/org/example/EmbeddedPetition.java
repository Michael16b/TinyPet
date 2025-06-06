package org.example;

import com.google.appengine.api.datastore.Entity;

import java.util.Date;
import java.util.List;


    public class EmbeddedPetition {
        public long id;
        public String title;
        public String content;
        public List<String> tags;
        public Date creationDate;
        public String creatorFirstName;
        public String creatorLastName;
        public long signatureCount;
        
            public EmbeddedPetition(Entity petition) {
                this.id = petition.getKey().getId();
                this.title = (String) petition.getProperty("title");
                this.content = (String) petition.getProperty("content");
                this.tags = (List<String>) petition.getProperty("tags");
                this.creationDate = (Date) petition.getProperty("creationDate");
                this.signatureCount = (Long) petition.getProperty("signatureCount");
        
                // ✅ Lire depuis l'entité Petition directement
                this.creatorFirstName = (String) petition.getProperty("creatorFirstName");
                this.creatorLastName = (String) petition.getProperty("creatorLastName");
            }
        }
        
    

