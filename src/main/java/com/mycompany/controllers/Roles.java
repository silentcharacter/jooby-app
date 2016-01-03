package com.mycompany.controllers;

import com.mycompany.domain.Role;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jooby.Jooby;

public class Roles extends Jooby {

    {
        use("/api/roles")
                .put("/:id", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection roles = jongo.getCollection("roles");
                    Role role = req.body().to(Role.class);
                    roles.save(role);
                    return role;
                })
                .get("/", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection roles = jongo.getCollection("roles");
                    return roles.find().as(Role.class);
                })
                .get("/:id", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection roles = jongo.getCollection("roles");
                    Role role = roles.findOne(new ObjectId(req.param("id").value())).as(Role.class);
                    return role;
                })
                .post("/", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection roles = jongo.getCollection("roles");
                    Role role = req.body().to(Role.class);
                    roles.insert(role);
                    return role;
                })
                .delete("/:id", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection roles = jongo.getCollection("roles");
                    roles.remove(new ObjectId(req.param("id").value()));
                    return req.param("id").value();
                });

    }
}
