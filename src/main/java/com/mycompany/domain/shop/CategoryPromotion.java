package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;
import org.apache.commons.lang3.StringUtils;


@JsonIgnoreProperties(ignoreUnknown = true)
@Deployment(table = "categoryPromotions")
public class CategoryPromotion extends Entity {

    public String name;
    public String message;
    public boolean active;
    public String categoryId;
    public String categoryCmsId;
    public int discountPercent;

    @Override
    public String getFullTextInner() {
        return StringUtils.defaultString(name) + StringUtils.defaultString(message);
    }
}
