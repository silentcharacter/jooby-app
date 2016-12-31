package com.mycompany.service;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
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
    @Inject
    private Jongo jongo;

    public AbstractService(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
        Deployment deployment = typeParameterClass.getAnnotation(Deployment.class);
        if (deployment == null) {
            throw new RuntimeException("Invalid entity - no deployment annotation!");
        }
        this.entityName = deployment.table();
    }

    private MongoCollection getCollection() {
        return jongo.getCollection(entityName);
    }

    public List<T> getAll() {
        return getAll(0, Integer.MAX_VALUE, "{_id: -1}", "{}", Collections.emptyList());
    }

    public List<T> getAll(Integer page, Integer perPage, String sort, String query, List<Object> filterValues) {
        MongoCursor<T> cursor = getCollection().find(query, filterValues.toArray())
                .sort(sort).limit(perPage).skip(page * perPage)
                .as(typeParameterClass);
        return Lists.newArrayList(cursor.iterator());
    }

    public T getById(String id) {
        return getCollection().findOne(new ObjectId(id)).as(typeParameterClass);
    }

    public T getBy(String field, String value) {
        return getCollection().findOne(String.format("{%s: '%s'}", field, value)).as(typeParameterClass);
    }

    public T insert(T obj) {
        getCollection().insert(obj);
        return obj;
    }

    public T update(T obj) {
        onSave(obj);
        getCollection().save(obj);
        return obj;
    }

    public void onSave(T object) {}
}
