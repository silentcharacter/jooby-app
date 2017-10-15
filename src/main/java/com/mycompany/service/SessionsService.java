package com.mycompany.service;

import com.google.inject.Inject;
import com.mycompany.domain.Session;
import org.jongo.Jongo;
import org.jooby.pac4j.AuthStore;
import org.pac4j.core.profile.CommonProfile;

import java.util.Date;
import java.util.Optional;


public class SessionsService {

    @Inject
    private AuthStore authStore;
    @Inject
    private Jongo jongo;

    @SuppressWarnings("unchecked")
    public SearchResult<Session> getActiveSessions(Integer page, Integer perPage) throws Exception {
        SearchResult<Session> searchResult = new SearchResult<>();
        org.jongo.MongoCollection col = jongo.getCollection("sessions");
        searchResult.count = col.count();
        searchResult.result = col.find().limit(perPage).skip((page - 1) * perPage).map(doc -> {
            Session session = new Session();
            session.id = (String) doc.get("_id");
            session._accessedAt = (Date) doc.get("_accessedAt");
            session._createdAt = (Date) doc.get("_createdAt");
            session._savedAt = (Date) doc.get("_savedAt");
            String prof = (String) doc.get("org．jooby．pac4j．Auth．id");
            session.auth = prof;
            try {
                Optional<CommonProfile> p = authStore.get(prof);
                if (p.isPresent()) {
                    session.email = p.get().getEmail();
                    session.name = p.get().getDisplayName();
                    session.roles = p.get().getRoles().toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return session;
        });
        return searchResult;
    }

}
