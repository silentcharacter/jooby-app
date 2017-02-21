package com.mycompany.domain;

import com.mycompany.annotation.Deployment;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Deployment(table = "users")
public class User extends Entity{

    public String profileId;
    public String firstName;
    public String lastName;
    public String email;
    public String street;
    public String city;
    public String zipcode;
    public String phone;
    public Date birthDate;
    public String password;
    public String passwordConfirm;
    public String website;
    public List<String> roles;

    public User() {
    }

    @Override
    public String getFullText() {
        return Optional.ofNullable(firstName).orElse("").toLowerCase() + " " + Optional.ofNullable(lastName).orElse("").toLowerCase();
    }
}
