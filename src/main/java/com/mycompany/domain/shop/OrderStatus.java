package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonValue;


public enum OrderStatus
{
	NEW("Новый"),

	PROCESSED("Обработан"),

	CANCELLED("Отменен"),

	IN_DELIVERY("В доставке"),

	DELIVERED("Доставлен"),

	NOT_DELIVERED("Не доставлен");

	private final String code;

	OrderStatus(String s)
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
