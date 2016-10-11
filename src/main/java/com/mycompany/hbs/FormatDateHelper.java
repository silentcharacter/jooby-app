package com.mycompany.hbs;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FormatDateHelper implements Helper<Date>
{
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

	@Override
	public CharSequence apply(Date param, Options options) throws IOException
	{
		if (param == null) {
			return "";
		}
		return dateFormat.format(param);
	}
}
