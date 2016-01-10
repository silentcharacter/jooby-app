package com.mycompany.domain;

import org.jongo.marshall.jackson.oid.ObjectId;

import java.util.Date;
import java.util.List;

public class User extends Entity{

    public String firstName;
    public String lastName;
    public String email;
    public String street;
    public String city;
    public String zipcode;
    public String phone;
    public Date birthDate;
    public String password;
    public String website;
    public List<ObjectId> roles;

    public User() {
    }

    @Override
    public String getFullText() {
        return firstName.toLowerCase() + " " + lastName.toLowerCase();
    }
}
