package com.mycompany.controller;

import com.mycompany.domain.ScriptLog;


public class ScriptLogs extends AbstractResource<ScriptLog> {

    public ScriptLogs() {
        super(ScriptLog.class);
    }

    {
        initializeRoutes();
    }
}
