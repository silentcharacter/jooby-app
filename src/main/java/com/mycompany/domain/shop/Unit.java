package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import org.apache.commons.lang3.StringUtils;


@Deployment(table = "units")
public class Unit extends Entity {

    public String name;
    public String label;
    public Integer coefficient;

    @Override
    public String getFullTextInner() {
        return StringUtils.defaultString(name);
    }
}
