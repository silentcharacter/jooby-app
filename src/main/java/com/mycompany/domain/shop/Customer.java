package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;

import java.util.Optional;


@Deployment(table = "customers")
public class Customer extends Entity
{
    public String name;
    public String comment;
    public String phone;
    public String address;

    @Override
    public String getFullText() {
        return Optional.ofNullable(name).orElse("").toLowerCase() + " " + Optional.ofNullable(comment).orElse("").toLowerCase()
            + " " + Optional.ofNullable(phone).orElse("").toLowerCase() + " " + Optional.ofNullable(address).orElse("").toLowerCase();
    }
}
