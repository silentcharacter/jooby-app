package com.mycompany;

import com.mycompany.controller.shop.*;
import com.mycompany.domain.shop.*;
import com.mycompany.service.shop.*;
import org.jooby.*;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.mycompany.constant.ShopAppConstants.CONTACT_BREADCRUMB;
import static com.mycompany.constant.ShopAppConstants.DELIVERY_BREADCRUMB;
import static com.mycompany.constant.ShopAppConstants.PAYMENT_BREADCRUMB;


public class ShopApp extends Jooby
{
	private static ProductService productService = new ProductService();
	private static ColorService colorService = new ColorService();
	private static SauceService sauceService = new SauceService();
	private static OrderService orderService = new OrderService();
	private static GlobalConfigService globalConfigService = new GlobalConfigService();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

	private static List<String> possibleDateTimes = new ArrayList<>();

	private static List<Sse> listeners = Collections.synchronizedList(new ArrayList<Sse>());

	{
		use(new Orders());
		use(new PaymentTypes());
		use(new DeliveryTypes());
		use(new Products());
		use(new Colors());
		use(new Sauces());
		use(new GlobalConfigs());

		possibleDateTimes.addAll(Arrays.asList("10:00-13:00", "13:00-16:00", "16:00-19:00", "19:00-22:00"));

		get("/shop", req -> Results.html("shop/shop")
				.put("templateName", "shop/main")
				.put("products", productService.getAll(req))
				.put("colors", colorService.getAll(req))
				.put("sauces", sauceService.getAll(req))
				.put("cart", CartService.getFetchedCart(req)));

		get("/cart", req -> Results.json(CartService.getFetchedCart(req)));

		post("/cart", req ->
		{
			Product product = productService.getById(req, req.param("productId").value());
			Color color = req.param("colorId").isSet() ? colorService.getById(req, req.param("colorId").value()) : null;
			List<Sauce> sauceList = new ArrayList<>();
			if (req.param("sauces[]").isSet())
			{
				sauceList.addAll(
						req.param("sauces[]").toList().stream().map(sauceId -> sauceService.getById(req, sauceId))
								.collect(Collectors.toList())
				);
			}
			return CartService.addToCart(req, product, req.param("quantity").intValue(), color, sauceList);
		});

		put("/cart", req ->
		{
			CartService.updateCartRow(req, req.param("entryNo").intValue(), req.param("quantity").intValue());
			return CartService.getFetchedCart(req);
		});

		delete("/cart", req -> CartService.removeFromCart(req, req.param("entryNo").intValue()));

		get("/shop/checkout/**", (req, rsp, chain) -> {
			//todo: optimize
			Cart cart = CartService.getSessionCart(req);
			if (cart.isEmpty()) {
				rsp.redirect("/shop");
			}
			chain.next(req, rsp);
		});

		get("/shop/checkout", req ->
		{
			Map cart = CartService.getFetchedCart(req);
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
						.put("cart", CartService.getFetchedCart(req));
			}
			CartService.saveContactInfo(req, cartForm);
			return Results.redirect("/shop/checkout/delivery");
		});

		get("/shop/checkout/delivery", req ->
		{
			Cart cart = CartService.getSessionCart(req);
			ValidationResult validationResult = OrderValidator.validateContacts(cart);
			if (!validationResult.equals(ValidationResult.OK))
			{
				return Results.redirect("/shop/checkout");
			}
			View view = Results.html("shop/checkout")
					.put("step", "delivery")
					.put("cart", CartService.getFetchedCart(req))
					.put("templateName", "shop/delivery")
					.put("breadcrumbs", DELIVERY_BREADCRUMB);
			populateDatesAndTimes(view, req);
			return view;
		});

		post("/shop/checkout/delivery", req ->
		{
			if (req.param("deliveryDate").isSet()) {
				CartService.setDeliveryOptions(req,
						req.param("delivery").value(),
						dateFormat.parse(req.param("deliveryDate").value()),
						req.param("deliveryTime").value());
			} else {
				CartService.setDeliveryOptions(req, req.param("delivery").value(), null, null);
			}
			return Results.redirect("/shop/checkout/payment");
		});

		get("/shop/checkout/payment", req -> Results.html("shop/checkout")
				.put("step", "payment")
				.put("cart", CartService.getFetchedCart(req))
				.put("templateName", "shop/payment")
				.put("breadcrumbs", PAYMENT_BREADCRUMB));

		post("/shop/checkout/payment", req ->
		{
			CartService.setPaymentType(req, req.param("payment").value());
			Order order = orderService.placeOrder(req);
			CartService.emptyCart(req);
			sendEvent(order);
			if (order != null) {
				return Results.redirect("/shop/thankyou?order=" + order.orderNumber);
			}
			throw new RuntimeException("Order not placed");
		});

		get("/shop/thankyou", req -> {
				String orderNumber = req.param("order").value();
				return Results.html("shop/checkout")
					.put("cart", orderService.getBy("orderNumber", orderNumber, req))
					.put("step", "thankyou")
					.put("orderNumber", orderNumber)
					.put("templateName", "shop/thankyou");
		});

		get("/orderByPhone", request -> orderService.findByPhone(request, request.param("phone").value()));

		//ARM
		get("/shop/order/detailed/:id", request -> orderService.getFetchedOrder(request.param("id").value(), request));

		post("/shop/order/delivery", req -> {
			Map<String, Object> order = req.body().to(Map.class);
			return orderService.sendToDelivery(order, req);
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
		GlobalConfig config = globalConfigService.getAll(req).get(0);

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
