package com.mycompany.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class DateUtils
{
	private static SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat utc_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public static Date safeParseUTC(Object s) {
		utc_format.setTimeZone(TimeZone.getTimeZone("UTC"));
		try
		{
			Date localDate = FORMAT.parse((String) s);
			return format.parse(utc_format.format(localDate));
		}
		catch (ParseException e)
		{
			return new Date();
		}
	}
}
