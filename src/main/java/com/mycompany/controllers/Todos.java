package com.mycompany.controllers;

import com.mycompany.domain.Todo;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jooby.Jooby;

public class Todos extends AbstractResource<Todo> {

    public Todos() {
        super(Todo.class, "todos");
    }

    {
        initializeRoutes();
    }
}
