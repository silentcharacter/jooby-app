package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import org.apache.commons.lang3.StringUtils;


@Deployment(table = "tags")
public class Tag extends Entity {

    public String name;

    @Override
    public String getFullTextInner() {
        return StringUtils.defaultString(name);
    }
}
