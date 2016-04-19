package com.mycompany.service.shop;

import com.mycompany.domain.shop.Color;
import com.mycompany.domain.shop.Product;
import com.mycompany.service.AbstractService;

public class ColorService extends AbstractService<Color> {

    public ColorService() {
        super(Color.class, "colors");
    }

}
