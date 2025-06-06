package org.example;

import com.google.appengine.api.datastore.Entity;
import java.util.Date;
import java.util.List;


public class SignersResponse {
    public long total;
    public List<SignerInfo> signers;

    public SignersResponse(long total, List<SignerInfo> signers) {
        this.total = total;
        this.signers = signers;
    }
}
