package com.mycompany.domain.shop;

public class CartEntry {
    public Product product;
    public Integer quantity = 0;

    public CartEntry() {
    }

    public CartEntry(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }
}
