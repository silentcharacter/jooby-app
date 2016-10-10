package com.mycompany.domain;

import com.mycompany.annotation.Deployment;


@Deployment(table = "roles")
public class Role extends Entity {

    public String name;

    public Role() {
    }

    public Role(String id) {
        this.id = id;
    }

    @Override
    public String getFullText() {
        return name.toLowerCase();
    }
}
