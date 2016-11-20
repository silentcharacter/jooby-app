package com.mycompany;

import com.mycompany.controller.shop.*;
import com.mycompany.domain.shop.*;
import com.mycompany.service.shop.*;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.View;

import java.text.SimpleDateFormat;
import java.util.*;
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
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

	{
		use(new Orders());
		use(new PaymentTypes());
		use(new DeliveryTypes());
		use(new Products());
		use(new Colors());
		use(new Sauces());

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
			populateDatesAndTimes(view);
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
			Order order = CartService.placeOrder(req);
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
	}

	private void populateDatesAndTimes(View view)
	{
		List<String> dates = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < 4; i++)
		{
			calendar.add(Calendar.DATE, 1);
			dates.add(dateFormat.format(calendar.getTime()));
		}
		List<String> dateTimes = new ArrayList<>();
		dateTimes.add("10:00 - 13:00");
		dateTimes.add("13:00 - 16:00");
		dateTimes.add("16:00 - 19:00");
		dateTimes.add("19:00 - 22:00");
		view.put("dates", dates).put("dateTimes", dateTimes);
	}

}
