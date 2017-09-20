package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.Binary;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@Deployment(table = "products")
public class Product extends Entity {

    public String code;
    public String name;
    public String description;
    public String shortDescription;
    public Integer price;
    public Binary image;
    public String categoryId;
    public String unitId;
    public List<String> tags;
    public boolean active;
    public boolean additional;

    @Override
    public String getFullText() {
        return StringUtils.defaultString(code).toLowerCase() + " " + StringUtils.defaultString(name).toLowerCase() + " " + StringUtils.defaultString(description).toLowerCase();
    }
}
