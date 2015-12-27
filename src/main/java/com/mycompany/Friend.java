package com.mycompany;

import org.jongo.marshall.jackson.oid.MongoObjectId;

public class Friend {
    @MongoObjectId
    public String _id;
    public String name;
    public Integer age;

    public Friend() {
    }

}
