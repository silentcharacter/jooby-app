package com.mycompany.hbs;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class EqualHelper implements Helper<String>
{
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

	@Override
	public CharSequence apply(String param, Options options) throws IOException
	{
		if (param == null || options.param(0) == null) {
			return options.inverse(this);
		}
		if (options.param(0) instanceof Date) {
			if (param.equals(dateFormat.format(options.param(0)))) {
				return options.fn(this);
			}
		} else if (param.equals(options.param(0))) {
			return options.fn(this);
		}
		return options.inverse(this);
	}
}
