package com.mycompany.controller.shop;

import com.mycompany.domain.shop.Cart;
import com.mycompany.domain.shop.Order;
import org.apache.commons.lang3.StringUtils;


public class OrderValidator
{
	public static ValidationResult validateContacts(Cart order) {
		if (StringUtils.isEmpty(order.phone)) {
			return new ValidationResult("phone", "Заполните поле 'Телефон'");
		}
		if (order.phone.replaceAll("[^\\d.]", "").length() != 11) {
			return new ValidationResult("phone", "Неверный номер телефона");
		}
		if (StringUtils.isEmpty(order.name)) {
			return new ValidationResult("name", "Заполните поле 'Имя'");
		}
		if (StringUtils.isEmpty(order.streetName)) {
			return new ValidationResult("streetName", "Заполните поле 'Улица'");
		}
		if (StringUtils.isEmpty(order.originalStreetNumber)) {
			return new ValidationResult("originalStreetNumber", "Заполните поле 'Номер дома'");
		}
		return ValidationResult.OK;
	}
}
