package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import org.bson.types.Binary;

import java.util.List;
import java.util.Optional;


@JsonIgnoreProperties(ignoreUnknown = true)
@Deployment(table = "medias")
public class Media extends Entity {

    public String code;
    public String description;
    public Binary image;

    @Override
    public String getFullText() {
        return Optional.ofNullable(code).orElse("").toLowerCase() + " " + Optional.ofNullable(description).orElse("").toLowerCase();
    }
}
