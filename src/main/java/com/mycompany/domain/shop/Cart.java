package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mycompany.domain.Entity;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Cart extends Entity
{
    public String name;
    public String phone;
    public String streetName;
    public Integer streetNumber;
    public String originalStreetNumber;
    public String litera;
    public Integer korpus;
    public int entrance;
    public Integer flat;
    public String deliveryId;
    public Integer deliveryPrice;
    public Date deliveryDate;
    public String deliveryTime;
    public String paymentTypeId;
    public Integer subTotalPrice = 0;
    public Integer totalPrice = 0;
    public Integer totalCount = 0;
    public List<OrderEntry> entries = new ArrayList<>();

    public void setEntrance(String v) {
        if (StringUtils.isNotEmpty(v)) {
            entrance = Integer.valueOf(v);
        }
    }
    public void setFlat(String v) {
        if (StringUtils.isNotEmpty(v)) {
            flat = Integer.valueOf(v);
        }
    }
    public void setStreetNumber(String v) {
        if (StringUtils.isNotEmpty(v)) {
            streetNumber = Integer.valueOf(v);
        }
    }

    public Integer getEntrance() {
        return entrance != 0 ? entrance : null;
    }

    @Override
    public String getFullText()
    {
        return "";
    }

    public void calculate() {
        totalPrice = 0;
        subTotalPrice = 0;
        totalCount = 0;
        int i = 0;
        for (OrderEntry entry : entries) {
            int rowTotalPrice = entry.productPrice * entry.quantity;
            rowTotalPrice += entry.colorPrice;
            rowTotalPrice += entry.saucePrice;
            entry.totalPrice = rowTotalPrice;
            subTotalPrice += rowTotalPrice;
            totalCount += entry.quantity;
            entry.entryNo = i++;
        }
        totalPrice = subTotalPrice + deliveryPrice;
    }

    public void addEntry(Product product, Integer quantity, Color color, List<Sauce> sauces) {
        Optional<OrderEntry> entry = entries.stream().filter(e -> e.productId.equals(product.id)).findAny();
        String colorId = color == null? "" : color.id;
        if (!entry.isPresent() || !colorId.equals(Optional.ofNullable(entry.get().colorId).orElse(""))) {
            entries.add(new OrderEntry(product, quantity, color, sauces, entries.size()));
        } else {
            entry.get().quantity += quantity;
        }
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

    public void removeEntry(Integer entryNo) {
        OrderEntry toRemove = null;
        for (OrderEntry entry : entries) {
            if (entry.entryNo.equals(entryNo)) {
                toRemove = entry;
                break;
            }
        }
        if (toRemove != null) {
            entries.remove(toRemove);
        }
        calculate();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public void setDelivery(Map delivery) {
        deliveryId = (String) delivery.get("id");
    }

    public void setPaymentType(Map paymentType) {
        paymentTypeId = (String) paymentType.get("id");
    }
}
