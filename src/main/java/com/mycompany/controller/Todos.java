package com.mycompany.controller;

import com.mycompany.domain.Todo;

public class Todos extends AbstractResource<Todo> {

    public Todos() {
        super(Todo.class);
    }

    {
        initializeRoutes();
    }
}
