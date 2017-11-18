package com.mycompany.hbs;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.mycompany.domain.shop.Unit;

import java.io.IOException;
import java.util.Map;


public class UnitMapHelper implements Helper<Map>
{
	@Override
	public CharSequence apply(Map param, Options options) throws IOException
	{
		if (param == null || options.param(0) == null) {
			return "";
		}
		if (options.params.length > 1) {
			Unit unit = (Unit) param.get(options.params[0]);
			if ("label".equals(options.params[1]))
				return unit.label;
			if ("coefficient".equals(options.params[1]))
				return unit.coefficient.toString();
		}
		return param.get(options.params[0].toString()).toString();
	}
}
