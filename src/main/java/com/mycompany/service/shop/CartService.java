package com.mycompany.service.shop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.domain.shop.*;
import org.jooby.Request;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CartService {

    public static Cart getSessionCart(Request req) {
        Optional<String> cartJson = req.session().get("cart").toOptional();
        ObjectMapper mapper = new ObjectMapper();
        Cart cart;
        if (cartJson.isPresent()) {
            try {
                cart = mapper.readValue(cartJson.get(), Cart.class);
            } catch (IOException e) {
                cart = new Cart();
            }
        } else {
            cart = new Cart();
            saveSessionCart(req, cart);
        }
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
        cart.entries.add(new CartEntry(product, quantity, color, sauces));
        cart.calculate();
        saveSessionCart(req, cart);
        return cart;
    }

    public static Cart removeFromCart(Request req, String productId) {
        Cart cart = getSessionCart(req);
        CartEntry toRemove = null;
        for (CartEntry entry : cart.entries) {
            if (entry.product.id.equals(productId)) {
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
}
