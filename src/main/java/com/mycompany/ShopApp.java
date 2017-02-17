package com.mycompany;

import com.mycompany.controller.shop.*;
import com.mycompany.domain.shop.*;
import com.mycompany.service.SmsService;
import com.mycompany.service.shop.*;
import com.sun.org.apache.xpath.internal.operations.Or;
import com.typesafe.config.Config;
import org.jooby.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mycompany.constant.ShopAppConstants.*;


public class ShopApp extends Jooby
{
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

	private static List<String> possibleDateTimes = new ArrayList<String>(){{
		addAll(Arrays.asList("10:00-13:00", "13:00-16:00", "16:00-19:00", "19:00-22:00"));
	}};

	private static Logger logger = LoggerFactory.getLogger(ShopApp.class);

	private static List<Sse> listeners = Collections.synchronizedList(new ArrayList<Sse>());

	private static CartService cartService;
	private static OrderService orderService;
	private static GoogleMapsService googleMapsService;
	private static SmsService smsService;

	{
//		use(new Orders());
		use(new PaymentTypes());
		use(new DeliveryTypes());
		use(new Products());
		use(new Colors());
		use(new Sauces());
		use(new GlobalConfigs());

		onStart(registry -> {
			cartService = registry.require(CartService.class);
			orderService = registry.require(OrderService.class);
			googleMapsService = registry.require(GoogleMapsService.class);
			smsService = registry.require(SmsService.class);
		});

		get("/", req -> Results.html("shop/shop")
				.put("templateName", "shop/main")
				.put("products", req.require(ProductService.class).getAll())
				.put("colors", req.require(ColorService.class).getAll())
				.put("sauces", req.require(SauceService.class).getAll())
				.put("cart", cartService.getFetchedCart(req)));

		get("/cart", req -> Results.json(cartService.getFetchedCart(req)));

		post("/cart", req ->
		{
			Product product = req.require(ProductService.class).getById(req.param("productId").value());
			Color color = req.param("colorId").isSet() ? req.require(ColorService.class).getById(req.param("colorId").value()) : null;
			List<Sauce> sauceList = new ArrayList<>();
			if (req.param("sauces[]").isSet())
			{
				SauceService sauceService = req.require(SauceService.class);
				sauceList.addAll(
						req.param("sauces[]").toList().stream().map(sauceService::getById)
								.collect(Collectors.toList())
				);
			}
			return cartService.addToCart(req, product, req.param("quantity").intValue(), color, sauceList);
		});

		put("/cart", req ->
		{
			cartService.updateCartRow(req, req.param("entryNo").intValue(), req.param("quantity").intValue());
			return cartService.getFetchedCart(req);
		});

		delete("/cart", req -> cartService.removeFromCart(req, req.param("entryNo").intValue()));

		get("/checkout/**", (req, rsp, chain) -> {
			//todo: optimize
			Cart cart = cartService.getSessionCart(req);
			if (cart.isEmpty()) {
				rsp.redirect("/");
			}
			chain.next(req, rsp);
		});

		get("/checkout", req ->
		{
			Map cart = cartService.getFetchedCart(req);
			return Results.html("shop/checkout")
					.put("cart", cart)
					.put("cartForm", cart)
					.put("step", "contact")
					.put("templateName", "shop/contacts")
					.put("breadcrumbs", CONTACT_BREADCRUMB);
		});

		post("/checkout", req ->
		{
			Cart cartForm = req.params(Cart.class);
			ValidationResult validationResult = OrderValidator.validateContacts(cartForm);
			if (!validationResult.equals(ValidationResult.OK))
			{
				return Results.html("shop/checkout")
						.put("templateName", "shop/contacts")
						.put("breadcrumbs", CONTACT_BREADCRUMB)
						.put("step", "contact")
						.put("cartForm", cartForm)
						.put("errorMessage", validationResult.message)
						.put("errorField", validationResult.fieldName)
						.put("cart", cartService.getFetchedCart(req));
			}
			cartService.saveContactInfo(req, cartForm);
			return Results.redirect("/checkout/delivery");
		});

		get("/checkout/delivery", req ->
		{
			Cart cart = cartService.getSessionCart(req);
			ValidationResult validationResult = OrderValidator.validateContacts(cart);
			if (!validationResult.equals(ValidationResult.OK))
			{
				return Results.redirect("/checkout");
			}
			View view = Results.html("shop/checkout")
					.put("step", "delivery")
					.put("cart", cartService.getFetchedCart(req))
					.put("templateName", "shop/delivery")
					.put("breadcrumbs", DELIVERY_BREADCRUMB);
			populateDatesAndTimes(view, req);
			return view;
		});

		post("/checkout/delivery", req ->
		{
			if (req.param("deliveryDate").isSet()) {
				cartService.setDeliveryOptions(req,
						req.param("delivery").value(),
						dateFormat.parse(req.param("deliveryDate").value()),
						req.param("deliveryTime").value());
			} else {
				cartService.setDeliveryOptions(req, req.param("delivery").value(), null, null);
			}
			return Results.redirect("/checkout/payment");
		});

		get("/checkout/payment", req -> Results.html("shop/checkout")
				.put("step", "payment")
				.put("cart", cartService.getFetchedCart(req))
				.put("templateName", "shop/payment")
				.put("breadcrumbs", PAYMENT_BREADCRUMB));

		post("/checkout/payment", req ->
		{
			cartService.setPaymentType(req, req.param("payment").value());
			OrderService orderService = req.require(OrderService.class);
			Order order = orderService.placeOrder(req);
			cartService.emptyCart(req);
			sendEvent(order);
			smsService.sendOrderConfirmationSms(order);
			if (order != null) {
				return Results.redirect("/thankyou?order=" + order.orderNumber);
			}
			throw new RuntimeException("Order not placed");
		});

		get("/thankyou", req -> {
				String orderNumber = req.param("order").value();
				return Results.html("shop/checkout")
					.put("cart", req.require(OrderService.class).getBy("orderNumber", orderNumber))
					.put("step", "thankyou")
					.put("orderNumber", orderNumber)
					.put("templateName", "shop/thankyou");
		});

		get("/orderByPhone", request -> orderService.findByPhone(request.param("phone").value()));

		//ARM
		get("/order/detailed/:id", req -> orderService.getFetchedOrder(req.param("id").value()));

		post("/order/delivery", req -> {
			Map<String, Object> order = req.body().to(Map.class);
			return orderService.sendToDelivery(order);
		});

		delete("/order/:id", req -> orderService.cancelOrder(req.param("id").value()));

		get("/coordinates/:streetName/:streetNumber", req -> {
			Geometry geometry = googleMapsService.getCoordinates(req.param("streetName").value(), req.param("streetNumber").value());
			if (geometry == null)
				return "";
			Map<String, String> res = new HashMap<>();
			res.put("lat", geometry.getLat());
			res.put("lng", geometry.getLng());
			return res;
		});

		get("/admin/schedule", req -> {
			String date = req.param("date").value();
			List<String> times = req.param("time").isSet()? Collections.singletonList(req.param("time").value()) : possibleDateTimes;
			Map<String, List<Map<String, Object>>> orders = new HashMap<>();
			for (String dateTime : times) {
				List<Map<String, Object>> orderList = orderService.getDeliverySchedule(date, dateTime);
				if (orderList.size() > 0) {
					orders.put(dateTime, orderList);
				}
			}
			Date d = (Date)orders.values().iterator().next().get(0).get("deliveryDate");
			return Results.html("/shop/schedule").put("date", d).put("orders", orders)
					.put("googleMapKey", req.require(Config.class).getString("google.map.key"));
		});

		sse("/events", sse -> {
			listeners.add(sse);
			sse.onClose(() -> listeners.remove(sse));
			sse.keepAlive(15, TimeUnit.SECONDS);
		});
	}

	private void sendEvent(Order order)
	{
		listeners.forEach(sse -> sse.send(order, "json"));
	}

	private void populateDatesAndTimes(View view, Request req)
	{
		GlobalConfig config = req.require(GlobalConfigService.class).getAll().get(0);

		List<String> tomorrowDateTimes = new ArrayList<>();
		Map<String, String> dates = new TreeMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1);
		Calendar deliveryPeriodStart = Calendar.getInstance();
		deliveryPeriodStart.add(Calendar.HOUR_OF_DAY, config.deliveryGap);
		//for tomorrow
		possibleDateTimes.forEach(entry -> {
			calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(entry.substring(0, 2)));
			if (deliveryPeriodStart.before(calendar)) {
				tomorrowDateTimes.add(entry);
			}
		});
		String date = dateFormat.format(calendar.getTime());
		if (!tomorrowDateTimes.isEmpty()) {
			dates.put(date, tomorrowDateTimes.stream().collect(Collectors.joining(",")));
		} else {
			tomorrowDateTimes.addAll(possibleDateTimes);
		}
		//next days
		for (int i = 1; i < config.deliveryDaysRange; i++)
		{
			calendar.add(Calendar.DATE, 1);
			date = dateFormat.format(calendar.getTime());
			dates.put(date, possibleDateTimes.stream().collect(Collectors.joining(",")));
		}
		view.put("dates", dates).put("tomorrowDateTimes", tomorrowDateTimes);
	}

}
