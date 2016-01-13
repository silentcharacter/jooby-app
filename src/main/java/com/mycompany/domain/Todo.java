package com.mycompany.domain;

import java.util.Date;

public class Todo extends Entity{

    public String title;
    public boolean done;
    public Date createdOn = new Date();
    public String user;

    @Override
    public String getFullText() {
        return title.toLowerCase();
    }

}
