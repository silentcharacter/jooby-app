package com.mycompany.service.shop;

import com.mycompany.domain.shop.Product;
import com.mycompany.service.AbstractService;

public class ProductService extends AbstractService<Product> {

    public ProductService() {
        super(Product.class, "products");
    }

}
