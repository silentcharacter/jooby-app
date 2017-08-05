package com.mycompany.hbs;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class MapHelper implements Helper<Map>
{
	@Override
	public CharSequence apply(Map param, Options options) throws IOException
	{
		if (param == null || options.param(0) == null) {
			return "";
		}
		return param.get(options.params[0].toString()).toString();
	}
}
