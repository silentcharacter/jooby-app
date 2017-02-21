package com.mycompany.controller;

import com.mycompany.domain.New;
import com.mycompany.service.NewsService;


public class News extends AbstractResource<New> {

    public News() {
        super(New.class, NewsService.class);
    }

}
