package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import org.apache.commons.lang3.StringUtils;


@JsonIgnoreProperties(ignoreUnknown = true)
@Deployment(table = "menus")
public class Menu extends Entity {

    public Integer index;
    public String name;
    public String url;
    public boolean mobile;

    @Override
    public String getFullTextInner() {
        return StringUtils.defaultString(name) + StringUtils.defaultString(url);
    }
}
