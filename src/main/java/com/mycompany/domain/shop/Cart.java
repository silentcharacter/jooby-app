package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mycompany.domain.Entity;

import java.util.ArrayList;
import java.util.List;

public class Cart extends Entity
{
    public static String FREE = "free";

    public String name;
    public String phone;
    public String streetName;
    public String streetNumber;
    public String entrance;
    public String flat;
    public String delivery;
    public String deliveryDate;
    public String deliveryTime;
    public String payment;

    public Cart()
    {
        delivery = FREE;
    }

    @Override
    public String getFullText()
    {
        return "";
    }

    public List<OrderEntry> entries = new ArrayList<>();
    public Integer deliveryPrice = 0;
    public Integer subTotalPrice = 0;
    public Integer totalPrice = 0;
    public Integer totalCount = 0;

    public void calculate() {
        totalPrice = 0;
        subTotalPrice = 0;
        totalCount = 0;
        for (OrderEntry entry : entries) {
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
            subTotalPrice += rowTotalPrice;
            totalCount += entry.quantity;
        }
        totalPrice = subTotalPrice + deliveryPrice;
    }

    public void addEntry(Product product, Integer quantity, Color color, List<Sauce> sauces) {
        entries.add(new OrderEntry(product, quantity, color, sauces, entries.size()));
        calculate();
    }

    public void updateEntry(Integer entryNo, Integer quantity) {
        OrderEntry cartEntry = null;
        for (OrderEntry entry : entries)
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
