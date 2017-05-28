package com.mycompany.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import com.mycompany.service.AbstractService;
import com.mycompany.service.SearchResult;
import com.mycompany.util.Utils;
import org.bson.types.ObjectId;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.Response;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AbstractResource<T extends Entity> extends Jooby {

    private static final String DATE_FILTER_POSTFIX = "_$";
    private Class<T> typeParameterClass = null;
    private Class<? extends AbstractService<T>> serviceClass = null;
    protected AbstractService<T> service = null;
    private String entityName = null;

    public AbstractResource(Class<T> typeParameterClass, Class<? extends AbstractService<T>> serviceClass) {
        this.typeParameterClass = typeParameterClass;
        Deployment deployment = typeParameterClass.getAnnotation(Deployment.class);
        if (deployment == null) {
            throw new RuntimeException("Invalid entity - no deployment annotation!");
        }
        this.entityName = deployment.table();
        this.serviceClass = serviceClass;

        initializeRoutes();
    }

    {
        onStart(registry -> service = registry.require(serviceClass));
    }

    private void initializeRoutes() {
        use("/api/" + entityName)
                .put("/:id", this::update)
                .get("/", this::getList)
                .get("/:id", this::getById)
                .post("/", this::insert)
                .delete("/:id", this::deleteHandler);
    }

    protected T update(Request req) throws Exception {
        return service.update(req.body().to(typeParameterClass));
    }

    protected T getById(Request req) {
        return service.getById(req.param("id").value());
    }

    protected T insert(Request req) throws Exception {
        return service.insert(req.body().to(typeParameterClass));
    }

    protected String deleteHandler(Request req) {
        return service.remove(req.param("id").value());
    }

    @SuppressWarnings("unchecked")
    protected void getList(Request req, Response rsp) throws Throwable
    {
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
                filterValues.add(String.format(".*%s.*", map.get("q").toString().toLowerCase().replace("+", "\\+")));
                map.remove("q");
            }

            Map<String, List<String>> groupedFields = map.keySet().stream()
                  .filter(key -> key.contains(DATE_FILTER_POSTFIX))
                  .collect(Collectors.groupingBy(fieldName -> fieldName.split("_")[0], Collectors.toList()));
            groupedFields.forEach((key, value) -> {
                queryConditions.add(key + ":" + value.stream().map(it -> it.split("_")[1] + ":#").collect(Collectors.joining(",", "{", "}")));
                filterValues.addAll(value.stream().map(v -> Utils.safeParseUTC(map.get(v))).collect(Collectors.toList()));
                value.forEach(map::remove);
            });

            map.forEach((key, value) -> {
                if (key.equals("$or")) {
                    Pattern pattern = Pattern.compile("(ISODate..)(\\d{4}-\\d{2}-\\d{2})");
                    String stringValue = value.toString();
                    Matcher matcher = pattern.matcher(stringValue);
                    while (matcher.find()) {
                        filterValues.add(Utils.safeParseUTC(matcher.group(2)));
                    }
                    queryConditions.add("$or:" + stringValue.replaceAll("ISODate\\('\\d{4}-\\d{2}-\\d{2}'\\)", "#"));
                    return;
                }
                if (value instanceof List) {
                    queryConditions.add(key + ": { $in:#}");
                } else {
                    queryConditions.add(key + ": #");
                }
                filterValues.add(extractValue(value));
            });
        }
        sendResult(req, rsp, page, perPage, filterValues, queryConditions);
    }

    protected void sendResult(Request req, Response rsp, Integer page, Integer perPage, List<Object> filterValues,
          List<String> queryConditions) throws Throwable
    {
        SearchResult<T> searchResult = service.getList(queryConditions, filterValues, extractSort(req),
              req.param("_count").isSet(), page, perPage);
        rsp.header("X-Total-Count", searchResult.count);
        if (searchResult.result != null) {
            rsp.send(searchResult.result);
        } else {
            rsp.send("");
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

    protected String extractSort(Request req) {
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

}
