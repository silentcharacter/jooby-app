package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;

import java.util.ArrayList;
import java.util.List;

@Deployment(table = "orderEntries")
public class OrderEntry extends Entity
{
    public Product product;
    public Color color;
    public List<Sauce> sauces = new ArrayList<>();
    public Integer quantity = 0;
    public Integer entryNo = 0;
    public Integer totalPrice = 0;

    public OrderEntry() {
    }

    @Override
    public String getFullText()
    {
        return product.getFullText();
    }

    public OrderEntry(Product product, Integer quantity, Color color, List<Sauce> sauces, Integer entryNo) {
        this.product = product;
        this.quantity = quantity;
        this.color = color;
        this.sauces = sauces;
        this.entryNo = entryNo;
    }
}
