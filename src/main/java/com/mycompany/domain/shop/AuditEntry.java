package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;

import java.util.Date;
import java.util.Map;

@Deployment(table = "audits")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditEntry extends Entity {

    public Date date;
    public String code;
    public String userName;
    public String userEmail;
    public String eventType;
    public String objectVersion;


    @Override
    public String getFullTextInner() {
        return code;
    }
}
