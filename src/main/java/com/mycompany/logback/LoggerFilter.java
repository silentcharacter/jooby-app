package com.mycompany.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;


public class LoggerFilter extends Filter<ILoggingEvent>
{
	private String logger;

	@Override
	public FilterReply decide(ILoggingEvent event)
	{
		if (!event.getLoggerName().startsWith(logger))
			return FilterReply.NEUTRAL;

		return FilterReply.DENY;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}
}
