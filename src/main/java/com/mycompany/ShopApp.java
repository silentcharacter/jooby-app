package com.mycompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.controller.shop.*;
import com.mycompany.domain.shop.*;
import com.mycompany.service.AuthenticationService;
import com.mycompany.service.EmailService;
import com.mycompany.service.ReviewService;
import com.mycompany.service.SmsService;
import com.mycompany.service.shop.*;
import com.mycompany.util.Utils;
import com.typesafe.config.Config;
import org.apache.commons.lang3.BooleanUtils;
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
	private static Set<String> excludedUrls = new HashSet<String>(){{
		addAll(Arrays.asList("/admin", "/api", "/media/image/banner", "/login", "/logout", "/google", "/auth", "/_log", "/maintenance"));
	}};

	private static Logger logger = LoggerFactory.getLogger(ShopApp.class);

	private static Set<Sse> listeners = Collections.synchronizedSet(new HashSet<Sse>());

	private static CartService cartService;
	private static OrderService orderService;
	private static GoogleMapsService googleMapsService;
	private static SmsService smsService;
	private static ProductService productService;
	private static ReviewService reviewService;
	private static MediaService mediaService;
	private static PromotionService promotionService;
	private static ColorService colorService;
	private static SauceService sauceService;
	private static MenuService menuService;
	private static CategoryService categoryService;
	private static TagService tagService;
	private static UnitService unitService;
	private static CmsPageService cmsPageService;
	private static EmailService emailService;
	private static DeliveryTypeService deliveryTypeService;
	private static Config config;
	private static GlobalConfigService globalConfigService;
	private static ObjectMapper mapper = new ObjectMapper();

	{
		onStart(registry -> {
			cartService = registry.require(CartService.class);
			orderService = registry.require(OrderService.class);
			googleMapsService = registry.require(GoogleMapsService.class);
			smsService = registry.require(SmsService.class);
			productService = registry.require(ProductService.class);
			mediaService = registry.require(MediaService.class);
			promotionService = registry.require(PromotionService.class);
			colorService = registry.require(ColorService.class);
			sauceService = registry.require(SauceService.class);
			cmsPageService = registry.require(CmsPageService.class);
			config = registry.require(Config.class);
			menuService = registry.require(MenuService.class);
			categoryService = registry.require(CategoryService.class);
			tagService = registry.require(TagService.class);
			unitService = registry.require(UnitService.class);
			reviewService = registry.require(ReviewService.class);
			emailService = registry.require(EmailService.class);
			deliveryTypeService = registry.require(DeliveryTypeService.class);
			globalConfigService = registry.require(GlobalConfigService.class);
		});

		err((req, rsp, err) -> {
			// do what ever you want here
			if (err.statusCode() == 403) {
				rsp.send(Results.html("shop/design")
						.put("templateName", "shop/error")
						.put("menus", menuService.getAll())
						.put("rootPath", SHOP_PATH)
						.put("cart", cartService.getFetchedCart(req)));
			}
			rsp.send(Results.html("shop/design")
					.put("templateName", "shop/error")
					.put("menus", menuService.getAll())
					.put("rootPath", SHOP_PATH)
					.put("cart", cartService.getFetchedCart(req)));
		});

		get("**", (req, rsp, chain) -> {
			if (BooleanUtils.isTrue(globalConfigService.getConfig().siteClosed) &&
					excludedUrls.stream().noneMatch(url -> req.path().startsWith(url))) {
				rsp.send(Results.html("shop/mockUp"));
			}
			chain.next(req, rsp);
		});

		get("/" + SHOP_PATH, req -> Results.html("shop/design")
				.put("templateName", "shop/main")
				.put("menus", menuService.getAll())
				.put("rootPath", SHOP_PATH)
				.put("categoryPromotion", promotionService.getBy("active", true))
				.put("popular", productService.aggregate(
						"{tags:'" + tagService.getPopular().id + "', active: true}", 3))
				.put("new", productService.aggregate(
						"{tags:'" + tagService.getNew().id + "', active: true}", 3))
				.put("units", unitService.getLabelsMap())
				.put("reviews", reviewService.getAll("{productId: null}"))
				.put("additional", productService.getAdditionalProducts())
				.put("categories", categoryService.getAllWithProducts())
				.put("analyticsKey", config.getString("google.analytics.key"))
				.put("cart", cartService.getFetchedCart(req)));

		get(SHOP_PATH + "/cms/:pageUrl", req -> Results.html("shop/design")
				.put("templateName", "shop/empty")
				.put("menus", menuService.getAll())
				.put("pageUrl", "/cms/" + req.param("pageUrl").value())
				.put("cmsPage", cmsPageService.getBy("url", "/"  + req.param("pageUrl").value()))
				.put("rootPath", SHOP_PATH)
				.put("cart", cartService.getFetchedCart(req)));

		get(SHOP_PATH + "/m-cart", req -> Results.html("shop/design")
				.put("templateName", "shop/cartMobile")
				.put("menus", menuService.getAll())
				.put("rootPath", SHOP_PATH)
				.put("cart", cartService.getFetchedCart(req))
				.put("categories", categoryService.getAll()));

//		get("/", req -> {
//			CommonProfile profile = AuthenticationService.getUserProfile(req);
//			if (profile == null) {
//				return Results.html("shop/mockUp").put("analyticsKey", config.getString("google.analytics.key"));
//			}
//			return Results.redirect(SHOP_PATH);
//		});

		get("/product/:productCode", req -> {
			Product product = productService.getBy("code", req.param("productCode").value());
			if (product == null) {
				product = productService.getById(req.param("productCode").value());
			}
			Category category = categoryService.getById(product.categoryId);
			return Results.html("shop/design")
					.put("templateName", "shop/product")
					.put("product", product)
					.put("cmsCategoryId", SHOP_PATH + "/#" + category.cmsId)
					.put("additional", productService.getAdditionalProducts())
					.put("unit", unitService.getById(product.unitId))
					.put("menus", menuService.getAll())
					.put("rootPath", SHOP_PATH)
					.put("cart", cartService.getFetchedCart(req));
		});

		get("/cart", req -> Results.json(cartService.getFetchedCart(req)));

		post("/cart", req -> {
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

		put("/cart", req -> {
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
						if (previousOrder.entrance != null)
							cart.put("entrance", Utils.formatEntrance(previousOrder.entrance));
						cart.put("flat", previousOrder.flat);
					}
				}
			}
			return Results.html("shop/checkout")
					.put("cart", cart)
					.put("menus", menuService.getAll())
					.put("menus", menuService.getAll())
					.put("rootPath", SHOP_PATH)
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
						.put("menus", menuService.getAll())
						.put("rootPath", SHOP_PATH)
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
					.put("menus", menuService.getAll())
					.put("payableDelivery", deliveryTypeService.getBy("name", DeliveryType.PAYABLE))
					.put("rootPath", SHOP_PATH)
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
			emailService.sendOrderPlacedEmail(order);
			return Results.redirect("/thankyou?order=" + order.orderNumber);
		});

		get("/testsms", req -> {
			Order order = new Order();
			order.orderNumber = "000001";
			order.phone = "+79066390408";
			smsService.sendOrderConfirmationSms(order, false);
			return "ok";
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
				.put("rootPath", SHOP_PATH)
				.put("cart", orderService.getFetchedOrderByNumber(orderNumber))
				.put("step", "thankyou")
				.put("menus", menuService.getAll())
				.put("orderNumber", orderNumber)
				.put("templateName", "shop/thankyou"));
		});

		get("/maintenance", req -> Results.html("shop/mockUp"));

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
			return orderService.updateOrder(order, req);
		});

		delete("/order/:id", req -> orderService.cancelOrder(req.param("id").value(), req));

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

		post("/image/:productId", req -> {
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
			rsp.header("Cache-Control", "max-age=36000, must-revalidate");
			if (image != null) {
				rsp.type("image/jpeg").send(image.getData());
			}
			rsp.send(Results.ok());
		});

		post("/review/image/:reviewId", req -> {
			try (Upload upload = req.file("file")) {
				FileInputStream inputStream = new FileInputStream(upload.file());
				byte b[] = new byte[inputStream.available()];
				inputStream.read(b);
				Review review = reviewService.getById(req.param("reviewId").value());
				review.image = new Binary(b);
				reviewService.update(review);
			} catch (IOException e) {
				logger.error("Error uploading image", e);
			}
			return Results.ok();
		});

		get("/review/image/:reviewId", (req, rsp) -> {
			Binary image = reviewService.getReviewImage(req.param("reviewId").value());
			rsp.header("Cache-Control", "max-age=36000, must-revalidate");
			if (image != null) {
				rsp.type("image/jpeg").send(image.getData());
			}
			rsp.send(Results.ok());
		});

		post("/media/:mediaId", req -> {
			try (Upload upload = req.file("file")) {
				FileInputStream inputStream = new FileInputStream(upload.file());
				byte b[] = new byte[inputStream.available()];
				inputStream.read(b);
				Media media = mediaService.getById(req.param("mediaId").value());
				media.image = new Binary(b);
				mediaService.update(media);
			} catch (IOException e) {
				logger.error("Error uploading image", e);
			}
			return Results.ok();
		});

		get("/media/image/:mediaCode", (req, rsp) -> {
			Binary image = mediaService.getImage(req.param("mediaCode").value());
			rsp.header("Cache-Control", "max-age=36000, must-revalidate");
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
		GlobalConfig config = globalConfigService.getConfig();

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
