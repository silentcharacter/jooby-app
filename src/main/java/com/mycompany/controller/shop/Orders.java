
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Order;
import com.mycompany.service.AbstractService;
import com.mycompany.service.shop.OrderService;
import org.jooby.Request;


public class Orders extends AbstractResource<Order> {

    public Orders() {
        super(Order.class);
    }
    {
        initializeRoutes();
    }

    @Override
    protected AbstractService<Order> getService(Request req)
    {
        return req.require(OrderService.class);
    }
}
