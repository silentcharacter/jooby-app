package com.mycompany.controllers;

import com.mycompany.domain.User;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jooby.Jooby;

public class Users extends Jooby {

    {
        use("/api/users")
                .put("/:id", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection users = jongo.getCollection("users");
                    User user = req.body().to(User.class);
                    users.save(user);
                    return user;
                })
                .get("/", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection users = jongo.getCollection("users");
                    return users.find().as(User.class);
                })
                .get("/:id", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection users = jongo.getCollection("users");
                    User user = users.findOne(new ObjectId(req.param("id").value())).as(User.class);
                    return user;
                })
                .post("/", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection users = jongo.getCollection("users");
                    User user = req.body().to(User.class);
                    users.insert(user);
                    return user;
                })
                .delete("/:id", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection users = jongo.getCollection("users");
                    users.remove(new ObjectId(req.param("id").value()));
                    return req.param("id").value();
                });

    }
}
