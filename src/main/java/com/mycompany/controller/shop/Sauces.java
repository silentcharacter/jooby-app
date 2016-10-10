package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Product;
import com.mycompany.domain.shop.Sauce;

public class Sauces extends AbstractResource<Sauce> {

    public Sauces() {
        super(Sauce.class);
    }

    {
        initializeRoutes();
    }
}
