package com.mycompany.domain;

import com.mycompany.annotation.Deployment;

import java.util.Date;

@Deployment(table = "scriptlogs")
public class ScriptLog extends Entity{

    public String version = "";
    public String description = "";
    public Date date = new Date();

    @Override
    public String getFullText() {
        return version.toLowerCase() + description.toLowerCase();
    }

}
