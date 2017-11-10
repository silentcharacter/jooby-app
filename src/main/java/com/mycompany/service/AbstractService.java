package com.mycompany.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jongo.bson.Bson;
import org.jongo.bson.BsonDocument;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public abstract class AbstractService<T extends Entity> {

    private Class<T> typeParameterClass = null;
    private String entityName = null;
    @Inject
    private Jongo jongo;
    private ObjectMapper mapper = new ObjectMapper();

    public AbstractService(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
        Deployment deployment = typeParameterClass.getAnnotation(Deployment.class);
        if (deployment == null) {
            throw new RuntimeException("Invalid entity - no deployment annotation!");
        }
        this.entityName = deployment.table();
    }

    protected MongoCollection getCollection() {
        return jongo.getCollection(entityName);
    }

    public List<T> getAll() {
        return getAll(0, Integer.MAX_VALUE, "{_id: -1}", "{}", Collections.emptyList());
    }

    public List<T> getAll(String query) {
        return getAll(0, Integer.MAX_VALUE, "{_id: -1}", query, Collections.emptyList());
    }

    public List<T> fullTextSearch(String q) {
        return getAll(0, Integer.MAX_VALUE, "{_id: -1}", "{fullText: {$regex: #}}",
              Collections.singletonList(String.format(".*%s.*", q)));
    }

    public List<T> getAll(Integer page, Integer perPage, String sort, String query, List<Object> filterValues) {
        MongoCursor<T> cursor = getCollection().find(query, filterValues.toArray())
                .sort(sort).limit(perPage).skip(page * perPage)
                .as(typeParameterClass);
        return Lists.newArrayList(cursor.iterator());
    }

    public SearchResult<T> getList(List<String> queryConditions, List<Object> filterValues, String sort, boolean onlyCount,
          int page, int perPage) throws Throwable {

        MongoCollection collection = jongo.getCollection(entityName);
        String query = queryConditions.stream().collect(Collectors.joining(",", "{", "}"));
        SearchResult<T> searchResult = new SearchResult<>();
        searchResult.count = collection.count(query, filterValues.toArray());
        if (!onlyCount) {
            searchResult.result = collection.find(query, filterValues.toArray()).sort(sort)
                  .limit(perPage).skip((page - 1) * perPage).map(dbObject -> {
							 BsonDocument bsonDocument = Bson.createDocument(dbObject);
							 return listReaderCallback(jongo.getMapper().getUnmarshaller().unmarshall(bsonDocument, typeParameterClass));
						});
        }
        return searchResult;
    }

    protected Object listReaderCallback(T t) {
        return t;
    }

    public T getById(String id) {
        return getCollection().findOne(new ObjectId(id)).as(typeParameterClass);
    }

    public T getBy(String field, String value) {
        return getCollection().findOne(String.format("{%s: '%s'}", field, value)).as(typeParameterClass);
    }

    public T getBy(String field, Boolean value) {
        return getCollection().findOne(String.format("{%s: %s}", field, value)).as(typeParameterClass);
    }

    public T insert(T obj) {
        obj.created = new Date();
        obj.modified = new Date();
        onSave(obj);
        getCollection().insert(obj);
        return obj;
    }

    public T update(T obj) {
        obj.modified = new Date();
        onSave(obj);
        getCollection().save(obj);
        return obj;
    }

    public void onSave(T object) {}

    public void onRemove(String id) {}

    public String remove(String id) {
        onRemove(id);
        getCollection().remove(new ObjectId(id));
        return id;
    }
}
