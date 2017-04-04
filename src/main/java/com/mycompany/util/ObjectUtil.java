package com.mycompany.util;

import java.util.Map;


public class ObjectUtil
{
	public static <T> T getValue(Map obj, String fieldName) {
		String[] fields = fieldName.split("\\.");
		if (fields.length > 1) {
			Map innerObj = (Map)obj.get(fields[0]);
			return (T) innerObj.get(fields[1]);
		}
		return (T) obj.get(fieldName);
	}
}
