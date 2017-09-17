package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Media;
import com.mycompany.service.shop.MediaService;


public class Medias extends AbstractResource<Media> {

    public Medias() {
        super(Media.class, MediaService.class);
    }

}
