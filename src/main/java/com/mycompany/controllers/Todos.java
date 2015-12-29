package com.mycompany.controllers;

import com.google.common.collect.Lists;
import com.mycompany.domain.Todo;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jooby.Jooby;

public class Todos extends Jooby {

    {
        use("/api/v1/todos")
                .put("/:id", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection todos = jongo.getCollection("todos");
                    Todo todo = todos.findOne(new ObjectId(req.param("id").value())).as(Todo.class);
                    todo.done = !todo.done;
                    todos.save(todo);
                    return todo;
                })
                .get("/", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection todos = jongo.getCollection("todos");
                    return todos.find().as(Todo.class);
                })
                .post("/", req -> {
                    Jongo jongo = req.require(Jongo.class);
                    MongoCollection todos = jongo.getCollection("todos");
                    Todo todo = req.body().to(Todo.class);
                    todos.insert(todo);
                    return todo;
                });

    }
}
