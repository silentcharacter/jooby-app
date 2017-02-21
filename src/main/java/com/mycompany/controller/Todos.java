package com.mycompany.controller;

import com.mycompany.domain.Todo;
import com.mycompany.service.TodoService;


public class Todos extends AbstractResource<Todo> {

    public Todos() {
        super(Todo.class, TodoService.class);
    }

}
