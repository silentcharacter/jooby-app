package com.mycompany.service;

import com.google.common.collect.Lists;
import com.google.inject.multibindings.StringMapKey;
import com.mycompany.annotation.Deployment;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.Route;

import java.util.Collections;
import java.util.List;

public abstract class AbstractService<T> {

    private Class<T> typeParameterClass = null;
    private String entityName = null;

    public AbstractService(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
        Deployment deployment = typeParameterClass.getAnnotation(Deployment.class);
        if (deployment == null) {
            throw new RuntimeException("Invalid entity - no deployment annotation!");
        }
        this.entityName = deployment.table();
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

    public T getBy(String field, String value, Request req) {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        return collection.findOne(String.format("{%s: '%s'}", field, value)).as(typeParameterClass);
    }

    public T insert(Request req, T obj) {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        collection.insert(obj);
        return obj;
    }
}
