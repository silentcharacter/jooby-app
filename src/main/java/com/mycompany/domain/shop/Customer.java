package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@JsonIgnoreProperties(ignoreUnknown = true)
@Deployment(table = "customers")
public class Customer extends Entity
{
    public String name;
    public String comment;
    public String phone;
    public List<Address> addresses = new ArrayList<>();

    @Override
    public String getFullText() {
        return Optional.ofNullable(name).orElse("").toLowerCase() + " " + Optional.ofNullable(comment).orElse("").toLowerCase()
            + " " + Optional.ofNullable(phone).orElse("").toLowerCase() + " " +
              addresses.stream().map(a->String.format("%s %s %s", a.streetName, a.streetNumber, a.flat)).collect(Collectors.joining(" "));
    }

}
