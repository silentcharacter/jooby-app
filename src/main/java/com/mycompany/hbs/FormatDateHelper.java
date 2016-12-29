package com.mycompany.hbs;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FormatDateHelper implements Helper<Object>
{
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

	@Override
	public CharSequence apply(Object param, Options options) throws IOException
	{
		if (param == null) {
			return "";
		}
		Date date = param instanceof Date? (Date)param : new Date((Long)param);
		return dateFormat.format(date);
	}
}
