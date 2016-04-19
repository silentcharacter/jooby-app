package com.mycompany;

import com.mycompany.controller.shop.Colors;
import com.mycompany.controller.shop.Products;
import com.mycompany.controller.shop.Sauces;
import com.mycompany.domain.shop.Color;
import com.mycompany.domain.shop.Product;
import com.mycompany.service.shop.CartService;
import com.mycompany.service.shop.ColorService;
import com.mycompany.service.shop.ProductService;
import com.mycompany.service.shop.SauceService;
import org.jooby.Jooby;
import org.jooby.Results;

public class ShopApp extends Jooby {

    {
        use(new ProductService());
        use(new ColorService());
        use(new SauceService());
        use(new Products());
        use(new Colors());
        use(new Sauces());

        get("/shop", req -> {
            ProductService productService = req.require(ProductService.class);
            return Results.html("shop")
                    .put("products", productService.getAll(req))
                    .put("cart", CartService.getSessionCart(req));
        });

        post("/addToCart", req -> {
            ProductService productService = req.require(ProductService.class);
            ColorService colorService = req.require(ColorService.class);
            Product product = productService.getById(req, req.param("productId").value());
            Color color = req.param("colorId").isSet() ? colorService.getById(req, req.param("colorId").value()) : null;
            return CartService.addToCart(req, product, req.param("quantity").intValue(), color, null);
        });

        post("/removeFromCart", req -> {
            return CartService.removeFromCart(req, req.param("productId").value());
        });

    }

}
