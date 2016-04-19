package com.mycompany.domain.shop;

import com.mycompany.domain.Entity;

import java.util.Optional;

public class Product extends Entity {

    public String name;
    public String description;
    public Integer price;

    @Override
    public String getFullText() {
        return Optional.ofNullable(name).orElse("").toLowerCase() + " " + Optional.ofNullable(description).orElse("").toLowerCase();
    }
}
