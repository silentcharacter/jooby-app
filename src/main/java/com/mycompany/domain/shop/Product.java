package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import org.bson.types.Binary;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@Deployment(table = "products")
public class Product extends Entity {

    public String name;
    public String description;
    public Integer price;
    public Binary image;
    public String categoryId;
    public String unitId;
    public List<String> tags;
    public boolean active;

    @Override
    public String getFullText() {
        return Optional.ofNullable(name).orElse("").toLowerCase() + " " + Optional.ofNullable(description).orElse("").toLowerCase();
    }
}
