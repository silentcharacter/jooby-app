package com.mycompany.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;


public class Utils
{
	private static SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat utc_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public static <T> T getValue(Map obj, String fieldName) {
		String[] fields = fieldName.split("\\.");
		if (fields.length > 1) {
			Map innerObj = (Map)obj.get(fields[0]);
			return (T) innerObj.get(fields[1]);
		}
		return (T) obj.get(fieldName);
	}

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

	public static String formatEntrance(int entrance) {
		return entrance == 0? "" : String.valueOf(entrance);
	}

	public static String formatPhone(String phone) {
		phone = phone.replaceAll("[^\\d+]", "");
		if (phone.startsWith("8") && phone.length() == 11) {
			phone = "+7" + phone.substring(1, phone.length());
		}
		if (phone.length() == 10) {
			phone = "+7" + phone;
		}

		return phone;
	}

}
