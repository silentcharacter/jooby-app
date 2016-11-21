package com.mycompany.service.shop;

import com.google.common.base.Strings;
import com.mongodb.client.MongoDatabase;
import com.mycompany.domain.shop.Order;
import com.mycompany.service.AbstractService;
import org.bson.Document;
import org.jooby.Request;

import java.util.HashMap;
import java.util.Map;


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

	public Map<String, String> findByPhone(Request req, String phone)
	{
		Map<String, String> map = new HashMap<>();
		if (Strings.isNullOrEmpty(phone)) {
			return map;
		}
		Order order = getBy("phone", phone, req);
		map.put("name", order.name);
		map.put("streetName", order.streetName);
		map.put("streetNumber", order.streetNumber);
		map.put("flat", order.flat);
		map.put("entrance", order.entrance);
		return map;
	}
}
