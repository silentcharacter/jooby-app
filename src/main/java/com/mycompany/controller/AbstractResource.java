package com.mycompany.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.annotation.Deployment;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.Route;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AbstractResource<T> extends Jooby {

    private static final String DATE_FILTER_POSTFIX = "_$";
    private SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private Class<T> typeParameterClass = null;
    private String entityName = null;

    public AbstractResource(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
        Deployment deployment = typeParameterClass.getAnnotation(Deployment.class);
        if (deployment == null) {
            throw new RuntimeException("Invalid entity - no deployment annotation!");
        }
        this.entityName = deployment.table();
    }

    protected void initializeRoutes() {
        use("/api/" + entityName)
                .put("/:id", updateHandler)
                .get("/", getListHandler)
                .get("/:id", getByIdHandler)
                .post("/", insertHandler)
                .delete("/:id", deleteHandler);
    }

    private Route.OneArgHandler updateHandler = req -> {
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
        List<Object> filterValues = new ArrayList<>();
        List<String> queryConditions = new ArrayList<>();
        if (req.param("_filters").isSet()) {
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, Object> map = mapper.readValue(req.param("_filters").value(), HashMap.class);

            if (map.containsKey("id")) {
                queryConditions.add("_id: {$in:#}");
                List<String> ids = (List<String>) map.get("id");
                filterValues.add(ids.stream().map(it -> new ObjectId(String.valueOf(it))).collect(Collectors.toList()));
                map.remove("id");
            }

            if (map.containsKey("q")) {
                queryConditions.add("fullText: {$regex: #}");
                filterValues.add(String.format(".*%s.*", map.get("q").toString().toLowerCase()));
                map.remove("q");
            }

            Map<String, List<String>> groupedFields = map.keySet().stream()
                  .filter(key -> key.contains(DATE_FILTER_POSTFIX))
                  .collect(Collectors.groupingBy(fieldName -> fieldName.split("_")[0], Collectors.toList()));
            groupedFields.forEach((key, value) -> {
                queryConditions.add(key + ":" + value.stream().map(it -> it.split("_")[1] + ":#").collect(Collectors.joining(",", "{", "}")));
                filterValues.addAll(value.stream().map(v -> safeParse(map.get(v))).collect(Collectors.toList()));
                value.forEach(map::remove);
            });

            map.forEach((key, value) -> {
                queryConditions.add(key + ": #");
                filterValues.add(extractValue(value));
            });
        }
        String query = queryConditions.stream().collect(Collectors.joining(",", "{", "}"));
        rsp.header("X-Total-Count", collection.count(query, filterValues.toArray()));
        if (!req.param("_count").isSet()) {
            rsp.send(collection.find(query, filterValues.toArray())
                  .sort(extractSort(req)).limit(perPage).skip((page - 1) * perPage)
                  .as(typeParameterClass));
        } else {
            rsp.send("");
        }
    };

    private Date safeParse(Object s) {
        SimpleDateFormat utc_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        utc_format.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try
        {
            Date localDate = FORMAT.parse((String) s);
            return format.parse(utc_format.format(localDate));
        }
        catch (ParseException e)
        {
            return new Date();
        }
    }


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

    private Route.OneArgHandler getByIdHandler = req -> {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        return collection.findOne(new ObjectId(req.param("id").value())).as(typeParameterClass);
    };

    private Route.OneArgHandler insertHandler = req -> {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        T object = req.body().to(typeParameterClass);
        collection.insert(object);
        return object;
    };

    private Route.OneArgHandler deleteHandler = req -> {
        Jongo jongo = req.require(Jongo.class);
        MongoCollection collection = jongo.getCollection(entityName);
        collection.remove(new ObjectId(req.param("id").value()));
        return req.param("id").value();
    };

}
