package com.mycompany.domain;

import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.Date;

public class Role {
    @MongoId
    @MongoObjectId
    public String id;
    public String name;

    public Role() {
    }

    public Role(String id) {
        this.id = id;
    }
}
