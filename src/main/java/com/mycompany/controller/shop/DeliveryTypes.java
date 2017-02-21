
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.DeliveryType;
import com.mycompany.service.shop.DeliveryTypeService;


public class DeliveryTypes extends AbstractResource<DeliveryType> {

    public DeliveryTypes() {
        super(DeliveryType.class, DeliveryTypeService.class);
    }
}
