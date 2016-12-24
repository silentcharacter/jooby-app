package com.mycompany.service.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.mongodb.client.MongoDatabase;
import com.mycompany.domain.shop.Cart;
import com.mycompany.domain.shop.Order;
import com.mycompany.domain.shop.OrderStatus;
import com.mycompany.service.AbstractService;
import org.bson.Document;
import org.jooby.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


public class OrderService extends AbstractService<Order>
{

	private final static Logger logger = LoggerFactory.getLogger(OrderService.class);

	private static ProductService productService = new ProductService();
	private static ColorService colorService = new ColorService();
	private static SauceService sauceService = new SauceService();
	private static DeliveryTypeService deliveryTypeService = new DeliveryTypeService();
	private static PaymentTypeService paymentTypeService = new PaymentTypeService();

	SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public OrderService()
	{
		super(Order.class);
	}

	private String generateNewOrderNumber(Request req)
	{
		MongoDatabase db = req.require(MongoDatabase.class);
		Document doc = db.runCommand(new Document("$eval", "getNextSequence('orderNumber')"));
		String result = String.valueOf(doc.getDouble("retval").intValue());
		while (result.length() < 8)
		{
			result = "0" + result;
		}
		return result;
	}

	public Map<String, String> findByPhone(Request req, String phone)
	{
		Map<String, String> map = new HashMap<>();
		if (Strings.isNullOrEmpty(phone))
		{
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

	public Order placeOrder(Request req)
	{
		Optional<String> cartJson = req.session().get("cart").toOptional();
		ObjectMapper mapper = new ObjectMapper();
		try
		{
			if (!cartJson.isPresent())
			{
				return null;
			}
			Order order = mapper.readValue(cartJson.get(), Order.class);
			order.orderNumber = generateNewOrderNumber(req);
			order.orderDate = new Date();
			order.status = OrderStatus.NEW;
			//needed to solve ng-admin bug not showing embedded linked entities
			order.sauces = sauceService.getAll(req).stream().map(sauce -> sauce.id).collect(Collectors.toList());
			insert(req, order);

			return order;
		}
		catch (IOException e)
		{
			logger.error("Error placing order", e);
		}
		return null;
	}

	//todo: move to facade
	public Map<String, Object> getFetchedOrder(String orderId, Request req)
	{
		return getOrderMap(req, getById(req, orderId));
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getOrderMap(Request req, Cart cart)
	{
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = objectMapper.convertValue(cart, Map.class);
		map.put("delivery", deliveryTypeService.getById(req, cart.deliveryId));
		map.put("paymentType", paymentTypeService.getById(req, cart.paymentTypeId));
		List<Map> entries = (List) map.get("entries");
		entries.forEach(entry ->
		{
			entry.put("product", productService.getById(req, (String) entry.get("productId")));
			entry.put("color", colorService.getById(req, (String) entry.get("colorId")));
			List<String> sauces = (List) entry.get("sauces");
			entry.put("sauces", sauces.stream().map(sauce -> sauceService.getById(req, sauce)).collect(Collectors.toList()));
		});
		return map;
	}

	public Map sendToDelivery(Map<String, Object> order, Request req) throws ParseException
	{
		Order saved = getById(req, (String) order.get("id"));
		saved.status = OrderStatus.IN_DELIVERY;
		saved.deliveryDate = FORMAT.parse((String) order.get("deliveryDate"));
		saved.deliveryTime = (String) order.get("deliveryTime");
		saved.streetName = (String) order.get("streetName");
		saved.streetNumber = (String) order.get("streetNumber");
		saved.entrance = (String) order.get("entrance");
		saved.flat = (String) order.get("flat");
		update(req, saved);
		return getFetchedOrder(saved.id, req);
	}
}
