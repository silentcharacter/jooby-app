
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Unit;
import com.mycompany.service.shop.UnitService;


public class Units extends AbstractResource<Unit> {

    public Units() {
        super(Unit.class, UnitService.class);
    }
}
