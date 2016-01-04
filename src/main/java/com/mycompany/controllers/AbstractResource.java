package com.mycompany.controllers;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jooby.Jooby;
import org.jooby.Route;

public class AbstractResource<T> extends Jooby {

    private Class<T> typeParameterClass = null;
    private String entityName = null;

    public AbstractResource(Class<T> typeParameterClass, String entityName) {
        this.typeParameterClass = typeParameterClass;
        this.entityName = entityName;
    }

    protected void initializeRoutes() {
        use("/api/" + entityName)
                .put("/:id",    updateHandler)
                .get("/",       getListHandler)
                .get("/:id",    getByIdHandler)
                .post("/",      insertHandler)
                .delete("/:id", deleteHandler);
    }

    protected Route.OneArgHandler updateHandler = req -> {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        T object = req.body().to(typeParameterClass);
        collection.save(object);
        return object;
    };

    protected Route.Handler getListHandler = (req, rsp) -> {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        String sortField = "id";
        Integer sortDir = -1;
        if (req.param("_sortField").isSet())
        {
            sortField = req.param("_sortField").value();
            sortDir = "DESC".equals(req.param("_sortDir").value())? -1 : 1;
        }
        Integer page = req.param("_page").intValue();
        Integer perPage = req.param("_perPage").intValue();
        rsp.header("X-Total-Count", collection.count());
        rsp.send(collection.find()
                .sort(String.format("{%s: %d}", sortField, sortDir))
                .limit(perPage).skip((page - 1) * perPage)
                .as(typeParameterClass));
    };

    protected Route.OneArgHandler getByIdHandler = req -> {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        return collection.findOne(new ObjectId(req.param("id").value())).as(typeParameterClass);
    };

    protected Route.OneArgHandler insertHandler = req -> {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        T object = req.body().to(typeParameterClass);
        collection.insert(object);
        return object;
    };

    protected Route.OneArgHandler deleteHandler = req -> {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        collection.remove(new ObjectId(req.param("id").value()));
        return req.param("id").value();
    };

}
