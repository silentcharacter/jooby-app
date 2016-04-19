package com.mycompany.domain.shop;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    public List<CartEntry> entries = new ArrayList<>();
    public Integer totalPrice = 0;

    public void calculate() {
        Integer totalPrice = 0;
        for (CartEntry entry : entries) {
            totalPrice += entry.product.price * entry.quantity;
            if (entry.color != null) {
                totalPrice += entry.color.price;
            }
            if (entry.sauces != null) {
                for (Sauce sauce : entry.sauces) {
                    totalPrice += sauce.price;
                }
            }
        }
        this.totalPrice = totalPrice;
    }
}
