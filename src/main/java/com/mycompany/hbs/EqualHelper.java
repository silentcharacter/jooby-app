package com.mycompany.hbs;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;


public class EqualHelper implements Helper<String>
{
	@Override
	public CharSequence apply(String param, Options options) throws IOException
	{
		if (param == null || options.param(0) == null) {
			return options.inverse(this);
		}
		if(param.equals(options.param(0))) {
			return options.fn(this);
		}
		return options.inverse(this);
	}
}
