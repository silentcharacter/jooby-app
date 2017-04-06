package com.mycompany.service.shop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.mycompany.domain.shop.*;
import com.mycompany.util.Utils;
import com.typesafe.config.Config;
import org.jooby.Request;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class CartService
{
	private ProductService productService;
	private ColorService colorService;
	private SauceService sauceService;
	private DeliveryTypeService deliveryTypeService;
	private PaymentTypeService paymentTypeService;

	@Inject
	public CartService(ProductService productService, ColorService colorService, SauceService sauceService,
			DeliveryTypeService deliveryTypeService, PaymentTypeService paymentTypeService)
	{
		this.productService = productService;
		this.colorService = colorService;
		this.sauceService = sauceService;
		this.deliveryTypeService = deliveryTypeService;
		this.paymentTypeService = paymentTypeService;
	}

	public Cart getSessionCart(Request req)
	{
		Optional<String> cartJson = req.session().get("cart").toOptional();
		ObjectMapper mapper = new ObjectMapper();
		Cart cart;
		try
		{
			if (cartJson.isPresent())
			{
				cart = mapper.readValue(cartJson.get(), Cart.class);
			}
			else
			{
				if ("dev".equals(req.require(Config.class).getString("application.env")))
				{
					cart = mapper.readValue(getDumpCartJson(), Cart.class);
				}
				else
				{
					cart = getNewCart();
				}
				saveSessionCart(req, cart);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			cart = getNewCart();
			saveSessionCart(req, cart);
		}
		return cart;
	}

	public Map<String, Object> getFetchedCart(Request req)
	{
		return req.require(OrderService.class).getOrderMap(getSessionCart(req));
	}

	private String getDumpCartJson() throws IOException
	{
		byte[] encoded = Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/public/shop/cart.json"));
		return new String(encoded, "utf-8");
	}

	public Cart getNewCart()
	{
		Cart cart = new Cart();
		DeliveryType deliveryType = deliveryTypeService.getBy("name", DeliveryType.FREE);
		cart.deliveryId = deliveryType.id;
		cart.deliveryPrice = deliveryType.price;
		cart.paymentTypeId = paymentTypeService.getBy("name", PaymentType.OFFLINE).id;
		return cart;
	}

	private void saveSessionCart(Request req, Cart cart)
	{
		String cartJsonString = "";
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			cartJsonString = mapper.writeValueAsString(cart);
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}
		req.session().set("cart", cartJsonString);
	}

	public void emptyCart(Request req)
	{
		saveSessionCart(req, getNewCart());
	}

	public Cart addToCart(Request req, Product product, Integer quantity, Color color, List<Sauce> sauces)
	{
		Cart cart = getSessionCart(req);
		cart.addEntry(product, quantity, color, sauces);
		saveSessionCart(req, cart);
		return cart;
	}

	public Cart updateCartRow(Request req, Integer entryNo, Integer quantity)
	{
		Cart cart = getSessionCart(req);
		cart.updateEntry(entryNo, quantity);
		saveSessionCart(req, cart);
		return cart;
	}

	public Cart removeFromCart(Request req, Integer entryNo)
	{
		Cart cart = getSessionCart(req);
		cart.removeEntry(entryNo);
		saveSessionCart(req, cart);
		return cart;
	}

	public void calculateCart(Cart cart) {
		cart.deliveryPrice = deliveryTypeService.getById(cart.deliveryId).price;
		if (deliveryTypeService.isFree(cart.deliveryId)) {
			cart.deliveryTime = DeliveryType.FREE_NAME;
		}
		cart.entries.forEach(e -> {
			e.productPrice = productService.getById(e.productId).price;
			if (e.colorId != null) {
				e.colorPrice = colorService.getById(e.colorId).price;
			}
			if (e.sauces != null) {
				e.saucePrice = e.sauces.stream().mapToInt(sauce -> sauceService.getById(sauce).price).sum();
			}
		});
		cart.calculate();
	}

	public void saveContactInfo(Request req, Cart cartForm)
	{
		Cart cart = getSessionCart(req);
		cart.name = cartForm.name;
		cart.phone = Utils.formatPhone(cartForm.phone);
		cart.streetName = cartForm.streetName;
		cart.streetNumber = cartForm.streetNumber;
		cart.entrance = cartForm.entrance;
		cart.flat = cartForm.flat;
		saveSessionCart(req, cart);
	}

	public void setDeliveryOptions(Request req, String deliveryType, Date deliveryDate, String deliveryTime)
	{
		Cart cart = getSessionCart(req);
		DeliveryType delivery = deliveryTypeService.getBy("name", deliveryType);
		cart.deliveryId = delivery.id;
		cart.deliveryPrice = delivery.price;
		cart.deliveryDate = deliveryDate;
		cart.deliveryTime = deliveryTime;
		cart.calculate();
		saveSessionCart(req, cart);
	}

	public void setPaymentType(Request req, String payment)
	{
		Cart cart = getSessionCart(req);
		cart.paymentTypeId = paymentTypeService.getBy("name", payment).id;
		saveSessionCart(req, cart);
	}



}
