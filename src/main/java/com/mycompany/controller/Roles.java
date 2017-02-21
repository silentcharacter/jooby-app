package com.mycompany.controller;

import com.mycompany.domain.Role;
import com.mycompany.service.RoleService;


public class Roles extends AbstractResource<Role> {

    public Roles() {
        super(Role.class, RoleService.class);
    }

}
