package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;

import java.util.Optional;


@Deployment(table = "addresses")
public class Address extends Entity
{
    public String streetName;
    public Integer streetNumber;
    public String litera;
    public Integer korpus;
    public Integer entrance;
    public Integer flat;

    public Address()
    {
    }

    public Address(String streetName, Integer streetNumber, String litera, Integer korpus, Integer entrance, Integer flat)
    {
        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.litera = litera;
        this.korpus = korpus;
        this.entrance = entrance;
        this.flat = flat;
    }

    @Override
    public String getFullText()
    {
        return Optional.ofNullable(streetName).orElse("").toLowerCase() + streetNumber + entrance + flat;
    }

}
