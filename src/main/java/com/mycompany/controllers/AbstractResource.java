package com.mycompany.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.Route;

import java.util.*;
import java.util.stream.Collectors;

public class AbstractResource<T> extends Jooby {

    private Class<T> typeParameterClass = null;
    private String entityName = null;

    public AbstractResource(Class<T> typeParameterClass, String entityName) {
        this.typeParameterClass = typeParameterClass;
        this.entityName = entityName;
    }

    protected void initializeRoutes() {
        use("/api/" + entityName)
                .put("/:id", updateHandler)
                .get("/", getListHandler)
                .get("/:id", getByIdHandler)
                .post("/", insertHandler)
                .delete("/:id", deleteHandler);
    }

    protected Route.OneArgHandler updateHandler = req -> {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        T object = req.body().to(typeParameterClass);
        collection.save(object);
        return object;
    };

    @SuppressWarnings("unchecked")
    protected Route.Handler getListHandler = (req, rsp) -> {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        Integer page = 1;
        Integer perPage = 10000;
        if (req.param("_page").isSet()) {
            page = req.param("_page").intValue();
            perPage = req.param("_perPage").intValue();
        }
        String query = "{%s}";
        List<Object> filterValues = new ArrayList<>();
        if (req.param("_filters").isSet()) {
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, Object> map = mapper.readValue(req.param("_filters").value(), HashMap.class);
            if (map.containsKey("id")) {
                List<String> ids = (List<String>) map.get("id");
                filterValues.add(ids.stream().map(it -> new ObjectId(String.valueOf(it))).collect(Collectors.toList()));
                query = String.format(query, "_id: {$in:#},%s");
            }
            if (map.containsKey("q")) {
                query = String.format(query, "fullText: {$regex: #},%s");
                filterValues.add(String.format(".*%s.*", map.get("q").toString().toLowerCase()));
            }
            map.remove("q");
            map.remove("id");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                query = String.format(query, entry.getKey() + ": #,%s");
                filterValues.add(extractValue(entry.getValue()));
            }
        }
        query = query.replace("%s", "").replace(",}", "}");
        rsp.header("X-Total-Count", collection.count(query, filterValues.toArray()));
        rsp.send(collection.find(query, filterValues.toArray())
                .sort(extractSort(req)).limit(perPage).skip((page - 1) * perPage)
                .as(typeParameterClass));
    };

    private Object extractValue(Object value) {
        if (!(value instanceof String)) {
            return value;
        }
        String str = value.toString();
        if (str.equals("true") || str.equals("false")) {
            return Boolean.valueOf(str);
        }
        return value;
    }

    private String extractSort(Request req) {
        String sort = "{_id: -1}";
        if (req.param("_sortField").isSet()) {
            sort = "{%s : %d %s}";
            Iterator<String> sortDirs = req.param("_sortDir").toList().iterator();
            for (String sortFieldName : req.param("_sortField").toList()) {
                if (sortFieldName.equals("id")) {
                    sortFieldName = "_id";
                }
                Integer sortDir = "DESC".equals(sortDirs.next()) ? -1 : 1;
                sort = String.format(sort, sortFieldName, sortDir,",%s : %d %s");
            }
            sort = sort.replace(",%s : %d %s", "");
        }
        return sort;
    }

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
