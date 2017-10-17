package com.mycompany.controller;

import com.mycompany.domain.shop.AuditEntry;
import com.mycompany.service.AuditService;
import org.jooby.Request;


public class Audits extends AbstractResource<AuditEntry> {

    public Audits() {
        super(AuditEntry.class, AuditService.class);
    }


    @Override
    protected AuditEntry update(Request req) throws Exception {
        return null;
    }

    @Override
    protected String deleteHandler(Request req) {
        return null;
    }

    @Override
    protected AuditEntry insert(Request req) throws Exception {
        return null;
    }
}
