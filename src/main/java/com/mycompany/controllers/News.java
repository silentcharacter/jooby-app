package com.mycompany.controllers;

import com.mycompany.domain.New;

public class News extends AbstractResource<New> {

    public News() {
        super(New.class, "news");
    }

    {
        initializeRoutes();
    }
}
