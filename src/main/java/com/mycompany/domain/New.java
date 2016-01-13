package com.mycompany.domain;

import java.util.Date;

public class New extends Entity{

    public String title;
    public String description;
    public Date createdOn = new Date();

    @Override
    public String getFullText() {
        return title.toLowerCase() + description.toLowerCase();
    }

}
