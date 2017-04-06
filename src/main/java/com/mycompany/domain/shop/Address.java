package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;

import java.util.Optional;


@Deployment(table = "addresses")
public class Address extends Entity
{
    public String streetName;
    public String streetNumber;
    public String entrance;
    public String flat;

    public Address()
    {
    }

    public Address(String streetName, String streetNumber, String entrance, String flat)
    {
        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.entrance = entrance;
        this.flat = flat;
    }

    @Override
    public String getFullText()
    {
        return Optional.ofNullable(streetName).orElse("").toLowerCase() + streetNumber + entrance + flat;
    }

}
