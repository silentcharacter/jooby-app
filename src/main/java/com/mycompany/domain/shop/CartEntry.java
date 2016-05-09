package com.mycompany.domain.shop;

import java.util.ArrayList;
import java.util.List;

public class CartEntry {
    public Product product;
    public Color color;
    public List<Sauce> sauces = new ArrayList<>();
    public Integer quantity = 0;
    public Integer entryNo = 0;
    public Integer totalPrice = 0;

    public CartEntry() {
    }

    public CartEntry(Product product, Integer quantity, Color color, List<Sauce> sauces, Integer entryNo) {
        this.product = product;
        this.quantity = quantity;
        this.color = color;
        this.sauces = sauces;
        this.entryNo = entryNo;
    }
}
