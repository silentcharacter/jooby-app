package com.mycompany;

import com.mycompany.controller.shop.*;
import com.mycompany.domain.shop.*;
import com.mycompany.service.shop.CartService;
import com.mycompany.service.shop.ColorService;
import com.mycompany.service.shop.ProductService;
import com.mycompany.service.shop.SauceService;
import org.jooby.Jooby;
import org.jooby.Results;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopApp extends Jooby {

    private static ProductService productService = new ProductService();
    private static ColorService colorService = new ColorService();
    private static SauceService sauceService = new SauceService();

    {
        use(new Products());
        use(new Colors());
        use(new Sauces());

        get("/shop", req -> Results.html("shop/shop")
					 .put("templateName", "shop/main")
					 .put("products", productService.getAll(req))
					 .put("colors", colorService.getAll(req))
					 .put("sauces", sauceService.getAll(req))
					 .put("cart", CartService.getSessionCart(req)));

        get("/cart", req -> Results.json(CartService.getSessionCart(req)));

        post("/cart", req -> {
            Product product = productService.getById(req, req.param("productId").value());
            Color color = req.param("colorId").isSet() ? colorService.getById(req, req.param("colorId").value()) : null;
            List<Sauce> sauceList = new ArrayList<>();
            if (req.param("sauces").isSet()) {
                sauceList.addAll(
                      req.param("sauces").toList().stream().map(sauceId -> sauceService.getById(req, sauceId))
                            .collect(Collectors.toList())
                );
            }
            return CartService.addToCart(req, product, req.param("quantity").intValue(), color, sauceList);
        });

        put("/cart", req -> CartService.updateCartRow(req, req.param("entryNo").intValue(), req.param("quantity").intValue()));

        delete("/cart", req -> CartService.removeFromCart(req, req.param("entryNo").intValue()));

        get("/shop/checkout", req -> {
            Cart order = new Cart();
            order.delivery = "freeDelivery";
            return Results.html("shop/checkout")
                  .put("order", order)
                  .put("cart", CartService.getSessionCart(req));
        });

        post("/shop/checkout", req -> {
            Cart order = req.body().to(Cart.class);
            ValidationResult validationResult = OrderValidator.validate(order);
            if (!validationResult.equals(ValidationResult.OK)) {
                return Results.html("shop/checkout")
                      .put("order", order)
                      .put("errorMessage", validationResult.message)
                      .put("errorField", validationResult.fieldName)
                      .put("cart", CartService.getSessionCart(req));
            }
            return Results.redirect("/shop/checkout/delivery");
        });

        get("/shop/checkout/delivery", req -> {
            Cart order = new Cart();
            order.delivery = "freeDelivery";
            return Results.html("shop/checkout")
                  .put("order", order)
                  .put("cart", CartService.getSessionCart(req));
        });

        post("/shop/checkout/delivery", req -> {
            Cart order = req.body().to(Cart.class);
            ValidationResult validationResult = OrderValidator.validate(order);
            if (!validationResult.equals(ValidationResult.OK)) {
                return Results.html("shop/checkout")
                      .put("order", order)
                      .put("errorMessage", validationResult.message)
                      .put("errorField", validationResult.fieldName)
                      .put("cart", CartService.getSessionCart(req));
            }
            return Results.redirect("/shop/checkout/delivery");
        });

        get("/shop/order/thankyou", req -> Results.html("shop/checkout").put("step", "thankyou"));

    }

}
