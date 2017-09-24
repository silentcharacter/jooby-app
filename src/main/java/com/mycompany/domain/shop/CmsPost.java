package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import org.apache.commons.lang3.StringUtils;


@JsonIgnoreProperties(ignoreUnknown = true)
@Deployment(table = "cmsposts")
public class CmsPost extends Entity {

    public String code;
    public String title;
    public String url;
    public boolean active;
    public String content;
    public boolean odd;
    public boolean first;

    @Override
    public String getFullTextInner() {
        return StringUtils.defaultString(code) + StringUtils.defaultString(url) + StringUtils.defaultString(title);
    }
}
