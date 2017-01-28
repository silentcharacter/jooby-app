package com.mycompany.hbs;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class IncHelper implements Helper<Integer>
{
	@Override
	public CharSequence apply(Integer param, Options options) throws IOException
	{
		if (param == null) {
			return "";
		}
		return String.valueOf(++param);
	}
}
