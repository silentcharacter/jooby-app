package com.mycompany.domain;

import com.mycompany.annotation.Deployment;

import java.util.Date;

@Deployment(table = "todos")
public class Todo extends Entity{

    public String title;
    public boolean done;
    public Date createdOn;
    public String user;

    @Override
    public String getFullText() {
        return title.toLowerCase();
    }

}
