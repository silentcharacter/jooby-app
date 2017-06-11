package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonValue;


public enum Channel
{
	PHONE("Телефон"),

	ONLINE("Сайт");

	private final String code;

	Channel(String s)
	{
		this.code = s;
	}

	@Override
	@JsonValue
	public String toString()
	{
		return code;
	}
}
