package com.mycompany.service.shop;

import com.mongodb.client.MongoDatabase;
import com.mycompany.domain.shop.Order;
import com.mycompany.service.AbstractService;
import org.bson.Document;
import org.jooby.Request;


public class OrderService extends AbstractService<Order> {

    public OrderService() {
        super(Order.class);
    }

    String generateNewOrderNumber(Request req) {
        MongoDatabase db = req.require(MongoDatabase.class);
        Document doc = db.runCommand(new Document("$eval", "getNextSequence('orderNumber')"));
        String result = String.valueOf(doc.getDouble("retval").intValue());
        while (result.length() < 8) {
            result = "0" + result;
        }
        return result;
    }

}
