package com.mycompany.controller;

import com.mycompany.domain.User;

public class Users extends AbstractResource<User> {

    public Users() {
        super(User.class);
    }

    {
        initializeRoutes();
    }
}
