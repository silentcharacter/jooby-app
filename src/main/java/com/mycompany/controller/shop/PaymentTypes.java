
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.PaymentType;
import com.mycompany.service.shop.PaymentTypeService;


public class PaymentTypes extends AbstractResource<PaymentType> {

    public PaymentTypes() {
        super(PaymentType.class, PaymentTypeService.class);
    }

}
