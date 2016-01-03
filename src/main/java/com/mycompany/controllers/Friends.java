package com.mycompany.controllers;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mycompany.domain.User;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jooby.mvc.Body;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;

import java.util.List;

@Path("/friends")
public class Friends {

    private Jongo jongo;

    @Inject
    public Friends(Jongo jongo) {
        this.jongo = jongo;
    }

    @GET
    public List<User> get() {
        MongoCollection friends = jongo.getCollection("friends");
        MongoCursor<User> all = friends.find().as(User.class);
        return Lists.newArrayList(all.iterator());
    }

    @Path("/:id")
    @GET
    public User get(String id) {
        MongoCollection friends = jongo.getCollection("friends");
        return friends.findOne(id).as(User.class);
    }

    @POST
    public User post(@Body User friend) {
        MongoCollection friends = jongo.getCollection("friends");
        friends.insert(friend);
        return friend;
    }
}
