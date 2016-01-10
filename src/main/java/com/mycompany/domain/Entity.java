package com.mycompany.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

public abstract class Entity {

    @MongoId
    @MongoObjectId
    public String id;

    @JsonProperty("fullText")
    public abstract String getFullText();

    public void setFullText(String s) {}

}
