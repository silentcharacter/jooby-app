
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.PaymentType;


public class PaymentTypes extends AbstractResource<PaymentType> {

    public PaymentTypes() {
        super(PaymentType.class);
    }

    {
        initializeRoutes();
    }
}
