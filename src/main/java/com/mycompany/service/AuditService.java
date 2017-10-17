package com.mycompany.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.domain.Entity;
import com.mycompany.domain.shop.AuditEntry;
import com.mycompany.domain.shop.Order;
import org.jooby.Request;
import org.jooby.internal.SessionManager;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


public class AuditService extends AbstractService<AuditEntry>
{
	private final static Logger logger = LoggerFactory.getLogger(AuditService.class);
	private ObjectMapper mapper = new ObjectMapper();

	public AuditService()
	{
		super(AuditEntry.class);
	}

	public void logEvent(Request request, Entity object) {
		AuditEntry entry = new AuditEntry();
		entry.date = new Date();
		entry.eventType = "update";
		if (object instanceof Order) {
			entry.code = ((Order)object).orderNumber;
		}
		try {
			entry.objectVersion = mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			logger.error("Error logging audit", e);
		}
		CommonProfile profile = AuthenticationService.getUserProfile(request);
		if (profile != null) {
			entry.userEmail = profile.getEmail();
			entry.userName = profile.getDisplayName();
		}
		insert(entry);
	}
}
