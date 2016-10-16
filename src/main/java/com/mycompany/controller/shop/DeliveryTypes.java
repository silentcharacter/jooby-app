
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.DeliveryType;


public class DeliveryTypes extends AbstractResource<DeliveryType> {

    public DeliveryTypes() {
        super(DeliveryType.class);
    }

    {
        initializeRoutes();
    }
}
