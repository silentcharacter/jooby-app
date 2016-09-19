package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    public List<CartEntry> entries = new ArrayList<>();
    public Integer totalPrice = 0;
    public Integer totalCount = 0;

    public void calculate() {
        totalPrice = 0;
        totalCount = 0;
        for (CartEntry entry : entries) {
            int rowTotalPrice = entry.product.price * entry.quantity;
            if (entry.color != null) {
                rowTotalPrice += entry.color.price;
            }
            if (entry.sauces != null) {
                for (Sauce sauce : entry.sauces) {
                    rowTotalPrice += sauce.price;
                }
            }
            entry.totalPrice = rowTotalPrice;
            totalPrice += rowTotalPrice;
            totalCount += entry.quantity;
        }
    }

    public void addEntry(Product product, Integer quantity, Color color, List<Sauce> sauces) {
        entries.add(new CartEntry(product, quantity, color, sauces, entries.size()));
        calculate();
    }

    public void updateEntry(Integer entryNo, Integer quantity) {
        CartEntry cartEntry = null;
        for (CartEntry entry : entries)
        {
            if (entry.entryNo.equals(entryNo)) {
                cartEntry = entry;
                break;
            }
        }
        if (cartEntry != null) {
            if (quantity == 0) {
                entries.remove(cartEntry);
            } else {
                cartEntry.quantity = quantity;
            }
        }
        calculate();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
