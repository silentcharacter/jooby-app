package com.mycompany.controller;

import com.mycompany.domain.ScriptLog;
import com.mycompany.service.ScriptLogService;


public class ScriptLogs extends AbstractResource<ScriptLog> {

    public ScriptLogs() {
        super(ScriptLog.class, ScriptLogService.class);
    }
}
