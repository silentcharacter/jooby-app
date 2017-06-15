package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.Binary;

import java.util.Optional;


@Deployment(table = "categories")
public class Category extends Entity {

    public String name;

    @Override
    public String getFullTextInner() {
        return StringUtils.defaultString(name);
    }
}
