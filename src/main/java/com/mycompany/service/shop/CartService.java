package com.mycompany.service.shop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.App;
import com.mycompany.domain.shop.*;
import com.typesafe.config.Config;
import org.jooby.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class CartService {

	 private final static Logger logger = LoggerFactory.getLogger(App.class);

    private static DeliveryTypeService deliveryTypeService = new DeliveryTypeService();
    private static PaymentTypeService paymentTypeService = new PaymentTypeService();
    private static OrderService orderService = new OrderService();

    public static Cart getSessionCart(Request req) {
        Optional<String> cartJson = req.session().get("cart").toOptional();
        ObjectMapper mapper = new ObjectMapper();
        Cart cart;
        try {
			  if (cartJson.isPresent()) {
					 cart = mapper.readValue(cartJson.get(), Cart.class);
			  } else {
					if ("dev".equals(req.require(Config.class).getString("application.env"))) {
						 cart = mapper.readValue(getDumpCartJson(), Cart.class);
					} else {
						 cart = getNewCart(req);
					}
					saveSessionCart(req, cart);
			  }
        } catch (IOException e) {
            e.printStackTrace();
            cart = getNewCart(req);
        }
        return cart;
    }

	static String getDumpCartJson() throws IOException
	{
		byte[] encoded = Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/public/shop/cart.json"));
		return new String(encoded, "utf-8");
	}

    private static Cart getNewCart(Request req)
    {
        Cart cart = new Cart();
        cart.delivery = deliveryTypeService.getBy("name", DeliveryType.FREE, req);
        cart.payment = paymentTypeService.getBy("name", PaymentType.OFFLINE, req);
        return cart;
    }

    private static void saveSessionCart(Request req, Cart cart) {
        String cartJsonString = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            cartJsonString = mapper.writeValueAsString(cart);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        req.session().set("cart", cartJsonString);
    }

    public static Cart addToCart(Request req, Product product, Integer quantity, Color color, List<Sauce> sauces) {
        Cart cart = getSessionCart(req);
        cart.addEntry(product, quantity, color, sauces);
        saveSessionCart(req, cart);
        return cart;
    }

    public static Cart updateCartRow(Request req, Integer entryNo, Integer quantity) {
        Cart cart = getSessionCart(req);
        cart.updateEntry(entryNo, quantity);
        saveSessionCart(req, cart);
        return cart;
    }

    public static Cart removeFromCart(Request req, Integer entryNo) {
        Cart cart = getSessionCart(req);
        OrderEntry toRemove = null;
        for (OrderEntry entry : cart.entries) {
            if (entry.entryNo.equals(entryNo)) {
                toRemove = entry;
                break;
            }
        }
        if (toRemove != null) {
            cart.entries.remove(toRemove);
        }
        cart.calculate();
        saveSessionCart(req, cart);
        return cart;
    }

    public static void saveContactInfo(Request req, Cart cartForm) {
        Cart cart = getSessionCart(req);
        cart.name = cartForm.name;
        cart.phone = cartForm.phone;
        cart.streetName = cartForm.streetName;
        cart.streetNumber = cartForm.streetNumber;
        cart.entrance = cartForm.entrance;
        cart.flat = cartForm.flat;
        saveSessionCart(req, cart);
    }

    public static void setDeliveryOptions(Request req, String deliveryType, Date deliveryDate, String deliveryTime) {
		 Cart cart = getSessionCart(req);
		 cart.delivery = deliveryTypeService.getBy("name", deliveryType, req);
		 cart.deliveryDate = deliveryDate;
		 cart.deliveryTime = deliveryTime;
		 cart.calculate();
		 saveSessionCart(req, cart);
	 }

    public static void setPaymentType(Request req, String payment)
    {
        Cart cart = getSessionCart(req);
        cart.payment = paymentTypeService.getBy("name", payment, req);
        saveSessionCart(req, cart);
    }

    public static Order placeOrder(Request req)
	 {
		 Optional<String> cartJson = req.session().get("cart").toOptional();
		 ObjectMapper mapper = new ObjectMapper();
		 try {
			 if (!cartJson.isPresent()) {
				 return null;
			 }
			 Order order = mapper.readValue(cartJson.get(), Order.class);
			 order.orderNumber = "123";
			 order.orderDate = new Date();
			 orderService.insert(req, order);
			 saveSessionCart(req, getNewCart(req));
			 return order;
		 } catch (IOException e) {
			 logger.error("Error placing order", e);
		 }
		 return null;
	 }

}
