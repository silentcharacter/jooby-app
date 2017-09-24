package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@Deployment(table = "cmspages")
public class CmsPage extends Entity {

    public String code;
    public String title;
    public String url;
    public boolean active;
    public String pageContent;
    public List<CmsPost> posts = new ArrayList<>();

    @Override
    public String getFullTextInner() {
        return StringUtils.defaultString(code) + StringUtils.defaultString(url) + StringUtils.defaultString(title);
    }
}
