package com.mycompany.domain;

import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.Date;

public class Todo {

    @MongoObjectId
    public String _id;
    public String title;
    public boolean done;
    public Date createdOn = new Date();

    public Todo() {
    }

    public Todo(String id, String title, boolean done, Date createdOn) {
        this._id = id;
        this.title = title;
        this.done = done;
        this.createdOn = createdOn;
    }

    public String getId() {
        return _id;
    }
}
