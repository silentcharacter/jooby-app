package com.mycompany.controllers;

import com.mycompany.domain.Role;

public class Roles extends AbstractResource<Role> {

    public Roles() {
        super(Role.class, "roles");
    }

    {
        initializeRoutes();
    }
}
