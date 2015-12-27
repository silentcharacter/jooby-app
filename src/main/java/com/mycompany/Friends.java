package com.mycompany;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
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
    public List<Friend> get() {
        MongoCollection friends = jongo.getCollection("friends");
        MongoCursor<Friend> all = friends.find().as(Friend.class);
        return Lists.newArrayList(all.iterator());
    }

    @Path("/:id")
    @GET
    public Friend get(String id) {
        MongoCollection friends = jongo.getCollection("friends");
        return friends.findOne(id).as(Friend.class);
    }

    @POST
    public Friend post(@Body Friend friend) {
        MongoCollection friends = jongo.getCollection("friends");
        friends.insert(friend);
        return friend;
    }
}
