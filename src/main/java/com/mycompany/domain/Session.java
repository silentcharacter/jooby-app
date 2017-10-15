package com.mycompany.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.annotation.Deployment;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Deployment(table = "sessions")
public class Session extends Entity{

    public String cart;
    public String pac4jUserProfile;
    public Date _accessedAt;
    public Date _createdAt;
    public Date _savedAt;
    public String auth;
    public String email;
    public String name;
    public String roles;

}
