package com.mycompany.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

public abstract class Entity {

    @MongoId
    @MongoObjectId
    public String id;

    @JsonProperty("fullText")
    public String getFullText() {
        return StringUtils.defaultString(getFullTextInner()).toLowerCase();
    }

    @JsonIgnore
    public String getFullTextInner() {
        return "";
    }

//    @JsonIgnore
//    @Override
//    public int hashCode()
//    {
//        return id.hashCode();
//    }
//
//    @JsonIgnore
//    @Override
//    public boolean equals(Object obj)
//    {
//        return obj instanceof Entity && StringUtils.defaultString(((Entity) obj).id).equals(id);
//    }

    public void setFullText(String s) {}

}
