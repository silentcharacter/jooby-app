package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Sauce;
import com.mycompany.service.shop.SauceService;


public class Sauces extends AbstractResource<Sauce> {

    public Sauces() {
        super(Sauce.class, SauceService.class);
    }
}
