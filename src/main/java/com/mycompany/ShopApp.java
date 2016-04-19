package com.mycompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.controller.shop.Products;
import com.mycompany.domain.shop.Cart;
import com.mycompany.domain.shop.CartEntry;
import com.mycompany.domain.shop.Product;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.Session;

import java.util.Optional;

public class ShopApp extends Jooby {

    {
        get("/shop", req -> {
            Jongo jongo = req.require(Jongo.class);
            MongoCollection collection = jongo.getCollection("products");
            MongoCursor products = collection.find().as(Product.class);

            Optional<String> cartJson = req.session().get("cart").toOptional();
            ObjectMapper mapper = new ObjectMapper();
            Cart cart = null;
            if (cartJson.isPresent()) {
                cart = mapper.readValue(cartJson.get(), Cart.class);
            } else {
                cart = new Cart();
                Product product = new Product();
                product.name = "test product";
                cart.entries.add(new CartEntry(product, 1));

                String cartJsonString = mapper.writeValueAsString(cart);
                req.session().set("cart", cartJsonString);
            }

            return Results.html("shop")
                    .put("products", products)
                    .put("cart", cart);
        });

        use(new Products());
    }

}
