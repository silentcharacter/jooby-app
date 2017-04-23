package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Deployment(table = "orderEntries")
public class OrderEntry extends Entity
{
    public String productId;
    public Integer productPrice = 0;
    public Integer colorPrice = 0;
    public Integer saucePrice = 0;
    public String colorId;
    public List<String> sauces = new ArrayList<>();
    public Integer quantity = 0;
    public Integer entryNo = 0;
    public Integer totalPrice = 0;

    public OrderEntry() {
    }

    @Override
    public String getFullText()
    {
        return productId;
    }

    public OrderEntry(Product product, Integer quantity, Color color, List<Sauce> sauces, Integer entryNo) {
        this.productId = product.id;
        this.productPrice = product.price;
        this.quantity = quantity;
        if (color != null) {
            this.colorId = color.id;
            this.colorPrice = color.price;
        }
        if (sauces != null) {
            this.saucePrice = sauces.stream().mapToInt(sauce -> sauce.price).sum();
            this.sauces = sauces.stream().map(sauce -> sauce.id).collect(Collectors.toList());
        }
        this.entryNo = entryNo;
    }

    public void setProduct(Map product) {
        productId = (String) product.get("id");
    }

    public void setColor(Map color) {
        colorId = (String) color.get("id");
    }

    public void setSauces(List<Map> sauces) {
        this.sauces = sauces.stream().map(el -> (String)el.get("id")).collect(Collectors.toList());
    }
}
