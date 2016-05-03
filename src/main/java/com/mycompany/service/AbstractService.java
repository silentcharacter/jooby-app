package com.mycompany.service;

import com.google.common.collect.Lists;
import com.google.inject.multibindings.StringMapKey;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jooby.Jooby;
import org.jooby.Request;

import java.util.Collections;
import java.util.List;

public abstract class AbstractService<T> extends Jooby {

    private Class<T> typeParameterClass = null;
    private String entityName = null;

    public AbstractService(Class<T> typeParameterClass, String entityName) {
        this.typeParameterClass = typeParameterClass;
        this.entityName = entityName;
    }

    public List<T> getAll(Request req) {
        return getAll(req, 0, Integer.MAX_VALUE, "{_id: -1}", "{}", Collections.emptyList());
    }

    public List<T> getAll(Request req, Integer page, Integer perPage, String sort, String query, List<Object> filterValues) {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        MongoCursor<T> cursor = collection.find(query, filterValues.toArray())
                .sort(sort).limit(perPage).skip(page * perPage)
                .as(typeParameterClass);
        return Lists.newArrayList(cursor.iterator());
    }

    public T getById(Request req, String id) {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        return collection.findOne(new ObjectId(id)).as(typeParameterClass);
    }
}
