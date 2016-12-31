package com.mycompany;

import com.google.inject.Inject;
import com.mycompany.controller.shop.*;
import com.mycompany.domain.shop.*;
import com.mycompany.service.shop.*;
import org.jooby.*;

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

	private static List<Sse> listeners = Collections.synchronizedList(new ArrayList<Sse>());

	{
		use(new Orders());
		use(new PaymentTypes());
		use(new DeliveryTypes());
		use(new Products());
		use(new Colors());
		use(new Sauces());
		use(new GlobalConfigs());

		get("/shop", req -> Results.html("shop/shop")
				.put("templateName", "shop/main")
				.put("products", req.require(ProductService.class).getAll())
				.put("colors", req.require(ColorService.class).getAll())
				.put("sauces", req.require(SauceService.class).getAll())
				.put("cart", req.require(CartService.class).getFetchedCart(req)));

		get("/cart", req -> Results.json(req.require(CartService.class).getFetchedCart(req)));

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
			return req.require(CartService.class).addToCart(req, product, req.param("quantity").intValue(), color, sauceList);
		});

		put("/cart", req ->
		{
			CartService cartService = req.require(CartService.class);
			cartService.updateCartRow(req, req.param("entryNo").intValue(), req.param("quantity").intValue());
			return cartService.getFetchedCart(req);
		});

		delete("/cart", req -> req.require(CartService.class).removeFromCart(req, req.param("entryNo").intValue()));

		get("/shop/checkout/**", (req, rsp, chain) -> {
			//todo: optimize
			Cart cart = req.require(CartService.class).getSessionCart(req);
			if (cart.isEmpty()) {
				rsp.redirect("/shop");
			}
			chain.next(req, rsp);
		});

		get("/shop/checkout", req ->
		{
			Map cart = req.require(CartService.class).getFetchedCart(req);
			return Results.html("shop/checkout")
					.put("cart", cart)
					.put("cartForm", cart)
					.put("step", "contact")
					.put("templateName", "shop/contacts")
					.put("breadcrumbs", CONTACT_BREADCRUMB);
		});

		post("/shop/checkout", req ->
		{
			Cart cartForm = req.body().to(Cart.class);
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
						.put("cart", req.require(CartService.class).getFetchedCart(req));
			}
			req.require(CartService.class).saveContactInfo(req, cartForm);
			return Results.redirect("/shop/checkout/delivery");
		});

		get("/shop/checkout/delivery", req ->
		{
			Cart cart = req.require(CartService.class).getSessionCart(req);
			ValidationResult validationResult = OrderValidator.validateContacts(cart);
			if (!validationResult.equals(ValidationResult.OK))
			{
				return Results.redirect("/shop/checkout");
			}
			View view = Results.html("shop/checkout")
					.put("step", "delivery")
					.put("cart", req.require(CartService.class).getFetchedCart(req))
					.put("templateName", "shop/delivery")
					.put("breadcrumbs", DELIVERY_BREADCRUMB);
			populateDatesAndTimes(view, req);
			return view;
		});

		post("/shop/checkout/delivery", req ->
		{
			CartService cartService = req.require(CartService.class);
			if (req.param("deliveryDate").isSet()) {
				cartService.setDeliveryOptions(req,
						req.param("delivery").value(),
						dateFormat.parse(req.param("deliveryDate").value()),
						req.param("deliveryTime").value());
			} else {
				cartService.setDeliveryOptions(req, req.param("delivery").value(), null, null);
			}
			return Results.redirect("/shop/checkout/payment");
		});

		get("/shop/checkout/payment", req -> Results.html("shop/checkout")
				.put("step", "payment")
				.put("cart", req.require(CartService.class).getFetchedCart(req))
				.put("templateName", "shop/payment")
				.put("breadcrumbs", PAYMENT_BREADCRUMB));

		post("/shop/checkout/payment", req ->
		{
			CartService cartService = req.require(CartService.class);
			cartService.setPaymentType(req, req.param("payment").value());
			OrderService orderService = req.require(OrderService.class);
			Order order = orderService.placeOrder(req);
			cartService.emptyCart(req);
			sendEvent(order);
			if (order != null) {
				return Results.redirect("/shop/thankyou?order=" + order.orderNumber);
			}
			throw new RuntimeException("Order not placed");
		});

		get("/shop/thankyou", req -> {
				String orderNumber = req.param("order").value();
				return Results.html("shop/checkout")
					.put("cart", req.require(OrderService.class).getBy("orderNumber", orderNumber))
					.put("step", "thankyou")
					.put("orderNumber", orderNumber)
					.put("templateName", "shop/thankyou");
		});

		get("/orderByPhone", request -> request.require(OrderService.class).findByPhone(request.param("phone").value()));

		//ARM
		get("/shop/order/detailed/:id", req -> req.require(OrderService.class).getFetchedOrder(req.param("id").value()));

		post("/shop/order/delivery", req -> {
			Map<String, Object> order = req.body().to(Map.class);
			return req.require(OrderService.class).sendToDelivery(order);
		});

		delete("/shop/order/:id", req -> req.require(OrderService.class).cancelOrder(req.param("id").value()));

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
