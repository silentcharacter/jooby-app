package com.mycompany.domain;

import com.mycompany.annotation.Deployment;

import java.util.Date;

@Deployment(table = "news")
public class New extends Entity{

    public String title;
    public String description;
    public Date createdOn = new Date();

    @Override
    public String getFullText() {
        return title.toLowerCase() + description.toLowerCase();
    }

}
