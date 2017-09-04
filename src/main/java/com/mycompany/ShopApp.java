package com.mycompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.constant.Constants;
import com.mycompany.controller.shop.*;
import com.mycompany.domain.shop.*;
import com.mycompany.service.AuthenticationService;
import com.mycompany.service.SmsService;
import com.mycompany.service.shop.*;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.Binary;
import org.jooby.*;
import org.mindrot.jbcrypt.BCrypt;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mycompany.constant.Constants.*;


public class ShopApp extends Jooby
{
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

	private static List<String> possibleDateTimes = new ArrayList<String>(){{
		addAll(Arrays.asList("10:00-13:00", "13:00-16:00", "16:00-19:00", "19:00-22:00"));
	}};

	private static Logger logger = LoggerFactory.getLogger(ShopApp.class);

	private static Set<Sse> listeners = Collections.synchronizedSet(new HashSet<Sse>());

	private static CartService cartService;
	private static OrderService orderService;
	private static GoogleMapsService googleMapsService;
	private static SmsService smsService;
	private static ProductService productService;
	private static ColorService colorService;
	private static SauceService sauceService;
	private static MenuService menuService;
	private static CategoryService categoryService;
	private static TagService tagService;
	private static UnitService unitService;
	private static Config config;
	private static ObjectMapper mapper = new ObjectMapper();

	{
//		use(new Orders());
		use(new PaymentTypes());
		use(new DeliveryTypes());
		use(new Products());
		use(new Colors());
		use(new Sauces());
		use(new GlobalConfigs());
		use(new Customers());
		use(new Districts());
		use(new Categories());
		use(new Tags());
		use(new Units());
		use(new Menus());

		onStart(registry -> {
			cartService = registry.require(CartService.class);
			orderService = registry.require(OrderService.class);
			googleMapsService = registry.require(GoogleMapsService.class);
			smsService = registry.require(SmsService.class);
			productService = registry.require(ProductService.class);
			colorService = registry.require(ColorService.class);
			sauceService = registry.require(SauceService.class);
			config = registry.require(Config.class);
			menuService = registry.require(MenuService.class);
			categoryService = registry.require(CategoryService.class);
			tagService = registry.require(TagService.class);
			unitService = registry.require(UnitService.class);
		});



		get(Constants.SHOP_PATH, req -> Results.html("shop/design")
				.put("templateName", "shop/main_new")
				.put("menus", menuService.getAll())
				.put("popular", productService.getAll("{tags:'" + tagService.getPopular().id + "'}"))
				.put("new", productService.getAll("{tags:'" + tagService.getNew().id + "'}"))
				.put("units", unitService.getLabelsMap())
				.put("fishDumplings", productService.getBy("name", "Пельмени с рыбой"))
				.put("humus", productService.getBy("name", "Хумус"))
				.put("additional", productService.getAdditionalProducts())
				.put("categories", categoryService.getAllWithProducts())
				.put("analyticsKey", config.getString("google.analytics.key"))
				.put("cart", cartService.getFetchedCart(req)));

		get(Constants.SHOP_PATH + "/delivery", req -> Results.html("shop/design")
				.put("templateName", "shop/empty")
				.put("menus", menuService.getAll())
				.put("categories", categoryService.getAll()));

		get(Constants.SHOP_PATH + "/blog", req -> Results.html("shop/design")
				.put("templateName", "shop/empty")
				.put("menus", menuService.getAll())
				.put("categories", categoryService.getAll()));

		get(Constants.SHOP_PATH + "/development", req -> Results.html("shop/design")
				.put("templateName", "shop/empty")
				.put("menus", menuService.getAll())
				.put("categories", categoryService.getAll()));

		get(Constants.SHOP_PATH + "/contacts", req -> Results.html("shop/design")
				.put("templateName", "shop/empty")
				.put("menus", menuService.getAll())
				.put("categories", categoryService.getAll()));

		get("/", req -> {
			CommonProfile profile = AuthenticationService.getUserProfile(req);
			if (profile == null) {
				return Results.html("shop/mockUp").put("analyticsKey", config.getString("google.analytics.key"));
			}
			return Results.html("shop/shop")
					.put("templateName", "shop/main")
					.put("products", productService.getAll("{active: true}"))
					.put("colors", colorService.getAll())
					.put("sauces", sauceService.getAll())
					.put("profile", profile)
					.put("analyticsKey", config.getString("google.analytics.key"))
					.put("cart", cartService.getFetchedCart(req));
		});

		get("/product/:productId", req -> {
			Product product = productService.getById(req.param("productId").value());
			return Results.html("shop/design")
					.put("templateName", "shop/product")
					.put("product", product)
					.put("additional", productService.getAdditionalProducts())
					.put("unit", unitService.getById(product.unitId))
					.put("menus", menuService.getAll())
					.put("cart", cartService.getFetchedCart(req));
		});

		get("/contacts", req -> Results.html("shop/shop")
				.put("templateName", "shop/contactsPage")
				.put("cart", cartService.getFetchedCart(req)));

		get("/cart", req -> Results.json(cartService.getFetchedCart(req)));

		post("/cart", req ->
		{
			Product product = productService.getById(req.param("productId").value());
			Color color = req.param("colorId").isSet() ? colorService.getById(req.param("colorId").value()) : null;
			List<Product> additions = new ArrayList<>();
			if (req.param("additions[]").isSet())
			{
				additions.addAll(
						req.param("additions[]").toList().stream().map(productService::getById)
								.collect(Collectors.toList())
				);
			}
			return cartService.addToCart(req, product, req.param("quantity").intValue(), color, additions);
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
			Map<String, Object> cart = cartService.getFetchedCart(req);
			if (req.cookie("foodsun").isSet() && StringUtils.isEmpty((String)cart.get("phone"))) {
				String cookie = req.cookie("foodsun").value();
				String[] decoded = new String(Base64.getDecoder().decode(cookie)).split(",");
				if (decoded.length == 2 && BCrypt.checkpw(decoded[0], decoded[1])) {
					Order previousOrder = orderService.getBy("orderNumber", decoded[0]);
					if (previousOrder != null) {
						cart.put("phone", previousOrder.phone.replace("+7", ""));
						cart.put("name", previousOrder.name);
						cart.put("streetName", previousOrder.streetName);
						cart.put("originalStreetNumber", previousOrder.originalStreetNumber);
						cart.put("entrance", previousOrder.entrance);
						cart.put("flat", previousOrder.flat);
					}
				}
			}
			return Results.html("shop/checkout")
					.put("cart", cart)
					.put("menus", menuService.getAll())
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
						.put("menus", menuService.getAll())
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
					.put("menus", menuService.getAll())
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
//			return Results.redirect("/checkout/payment");
			Order order = orderService.placeOrder(req);
			if (order == null) {
				throw new RuntimeException("Order not placed");
			}
			cartService.emptyCart(req);
			sendEvent(order);
			smsService.sendOrderConfirmationSms(order, false);
			return Results.redirect("/thankyou?order=" + order.orderNumber);
		});

//		get("/checkout/payment", req -> Results.html("shop/checkout")
//				.put("step", "payment")
//				.put("cart", cartService.getFetchedCart(req))
//				.put("shopId", config.getInt("yandex.shopId"))
//				.put("scid", config.getInt("yandex.scid"))
//				.put("templateName", "shop/payment")
//				.put("breadcrumbs", PAYMENT_BREADCRUMB));
//
//		post("/checkout/payment", req ->
//		{
//			cartService.setPaymentType(req, req.param("payment").value());
//			Order order = orderService.placeOrder(req);
//			if (order == null) {
//				throw new RuntimeException("Order not placed");
//			}
//			cartService.emptyCart(req);
//			sendEvent(order);
//			smsService.sendOrderConfirmationSms(order, false);
//			return Results.redirect("/thankyou?order=" + order.orderNumber);
//		});

		get("/thankyou", (req, rsp, chain) -> {
			String orderNumber = req.param("order").value();
			String salt = BCrypt.gensalt();
			String hash = BCrypt.hashpw(orderNumber, salt);
			String encoded = Base64.getEncoder().encodeToString(String.format("%s,%s", orderNumber, hash).getBytes());
			rsp.cookie("foodsun", encoded);
			rsp.send(Results.html("shop/checkout")
				.put("cart", orderService.getFetchedOrderByNumber(orderNumber))
				.put("step", "thankyou")
				.put("menus", menuService.getAll())
				.put("orderNumber", orderNumber)
				.put("templateName", "shop/thankyou"));
		});

		//ARM
		get("/order/detailed/:orderNumber", req -> {
			Order voiceOrder = null;
			if (req.session().get("voiceOrder").isSet()) {
				try {
					voiceOrder = mapper.readValue(req.session().get("voiceOrder").value(), Order.class);
				} finally {
					req.session().unset("voiceOrder");
				}
			}
			return orderService.getFetchedOrderByNumber(req.param("orderNumber").value(), voiceOrder);
		});

		post("/order/delivery", req -> {
			Map<String, Object> order = req.body().to(Map.class);
			return orderService.sendToDelivery(order);
		});

		delete("/order/:id", req -> orderService.cancelOrder(req.param("id").value()));

		post("/order/place", req-> {
			Order order = orderService.createOrder(req.body().to(Map.class));
			smsService.sendOrderConfirmationSms(order, true);
			return orderService.getFetchedOrder(order.id);
		});

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
			List<String> possibleTimes = new ArrayList<>(possibleDateTimes);
			possibleTimes.add("Бесплатная");
			List<String> times = req.param("time").isSet()? Collections.singletonList(req.param("time").value()) : possibleTimes;
			Map<String, List<Map<String, Object>>> orders = new HashMap<>();
			for (String dateTime : times) {
				List<Map<String, Object>> orderList = orderService.getDeliverySchedule(date, dateTime);
				if (orderList.size() > 0) {
					orders.put(dateTime, orderList);
				}
			}
			Date d = new Date((Long)orders.values().iterator().next().get(0).get("deliveryDate"));
			return Results.html("/shop/schedule").put("date", d).put("orders", orders)
					.put("googleMapKey", req.require(Config.class).getString("google.map.key"));
		});

		sse("/events", sse -> {
			listeners.add(sse);
			sse.onClose(() -> listeners.remove(sse));
			sse.keepAlive(15, TimeUnit.SECONDS);
		});

		post("/shop/image/:productId", req -> {
			try (Upload upload = req.file("file")) {
				FileInputStream inputStream = new FileInputStream(upload.file());
				byte b[] = new byte[inputStream.available()];
				inputStream.read(b);
				Product product = productService.getById(req.param("productId").value());
				product.image = new Binary(b);
				productService.update(product);
			} catch (IOException e) {
				logger.error("Error uploading image", e);
			}
			return Results.ok();
		});

		get("/image/product/:productId", (req, rsp) -> {
			Binary image = productService.getProductImage(req.param("productId").value());
			if (image != null) {
				rsp.type("image/jpeg").send(image.getData());
			}
			rsp.send(Results.ok());
		});

		get("/voice", req -> Results.html("/shop/voice"));
		post("/voice", req -> {
			Order order = orderService.parseOrderFromString(req.param("content").value());
			req.session().set("voiceOrder", mapper.writeValueAsString(order));
			return Results.redirect("/admin/arm/new");
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
