
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Order;


public class Orders extends AbstractResource<Order> {

    public Orders() {
        super(Order.class);
    }

    {
        initializeRoutes();
    }


}
