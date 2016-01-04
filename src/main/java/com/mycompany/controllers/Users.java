package com.mycompany.controllers;

import com.mycompany.domain.User;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.jongo.Find;
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
                .get("/", (req, rsp) -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection users = jongo.getCollection("users");
                    String sortField = req.param("_sortField").value();
                    Integer sortDir = "DESC".equals(req.param("_sortDir").value())? -1 : 1;
                    if (StringUtils.isEmpty(sortField))
                    {
                        sortField = "id";
                        sortDir = -1;
                    }
                    Integer page = req.param("_page").intValue();
                    Integer perPage = req.param("_perPage").intValue();
                    rsp.header("X-Total-Count", users.count());
                    rsp.send(users.find()
                            .sort(String.format("{%s: %d}", sortField, sortDir))
                            .limit(perPage).skip((page - 1) * perPage)
                            .as(User.class));
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
