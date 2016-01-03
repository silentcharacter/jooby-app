package com.mycompany.domain;

import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
import org.jongo.marshall.jackson.oid.ObjectId;

import java.util.Date;
import java.util.List;

public class User {
    @MongoId
    @MongoObjectId
    public String id;
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

}
