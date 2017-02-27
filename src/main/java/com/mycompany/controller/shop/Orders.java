
package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Order;
import com.mycompany.service.SearchResult;
import com.mycompany.service.shop.OrderService;
import org.jooby.Request;
import org.jooby.Response;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class Orders extends AbstractResource<Order> {

    public Orders() {
        super(Order.class, OrderService.class);
    }

    @Override
    protected void sendResult(Request req, Response rsp, Integer page, Integer perPage, List<Object> filterValues,
          List<String> queryConditions) throws Throwable
    {
        SearchResult<Order> searchResult = service.getList(queryConditions, filterValues, extractSort(req),
              req.param("_count").isSet(), page, perPage);
        rsp.header("X-Total-Count", searchResult.count);
        if (searchResult.result != null) {
            rsp.send(StreamSupport.stream(searchResult.result.spliterator(), false)
                  .map(o -> getOrderService().getOrderMap(o)).collect(Collectors.toList()));
        } else {
            rsp.send("");
        }
    }

    private OrderService getOrderService() {
        return (OrderService) service;
    }
}
