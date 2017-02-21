package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Product;
import com.mycompany.service.shop.ProductService;


public class Products extends AbstractResource<Product> {

    public Products() {
        super(Product.class, ProductService.class);
    }

}
