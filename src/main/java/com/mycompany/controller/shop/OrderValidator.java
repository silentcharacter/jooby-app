package com.mycompany.controller.shop;

import com.mycompany.domain.shop.Cart;
import com.mycompany.domain.shop.Order;
import org.apache.commons.lang3.StringUtils;


public class OrderValidator
{
	public static ValidationResult validate(Cart order) {
		if (StringUtils.isEmpty(order.phone)) {
			return new ValidationResult("phone", "Заполните поле 'Телефон'");
		}
		if (StringUtils.isEmpty(order.name)) {
			return new ValidationResult("name", "Заполните поле 'Имя'");
		}
		if (StringUtils.isEmpty(order.streetName)) {
			return new ValidationResult("streetName", "Заполните поле 'Улица'");
		}
		if (StringUtils.isEmpty(order.streetNumber)) {
			return new ValidationResult("streetNumber", "Заполните поле 'Номер дома'");
		}
		if (StringUtils.isEmpty(order.flat)) {
			return new ValidationResult("flat", "Заполните поле 'Квартира/офис'");
		}
//		if (order.delivery.equals("paidDelivery")) {
//			if (StringUtils.isEmpty(order.deliveryDate) || order.deliveryDate.equals("NULL")) {
//				return new ValidationResult("deliveryDate", "Выберите дату доставки");
//			}
//			if (StringUtils.isEmpty(order.deliveryTime) || order.deliveryTime.equals("NULL")) {
//				return new ValidationResult("deliveryTime", "Выберите время доставки");
//			}
//		}
		return ValidationResult.OK;
	}
}
