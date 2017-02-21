
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Color;
import com.mycompany.service.shop.ColorService;


public class Colors extends AbstractResource<Color> {

    public Colors() {
        super(Color.class, ColorService.class);
    }
}
