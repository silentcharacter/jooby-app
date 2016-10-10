package com.mycompany.constant;

import com.mycompany.domain.shop.Breadcrumb;

import java.util.Arrays;
import java.util.List;


public class ShopAppConstants
{
	public static final List<Breadcrumb> CONTACT_BREADCRUMB = Arrays.asList(
			new Breadcrumb(false, "/shop", "Каталог"),
			new Breadcrumb(true, null, "Контактная информация"),
			new Breadcrumb(false, "/shop/checkout/delivery", "Способ доставки"),
			new Breadcrumb(false, null, "Оплата")
	);

	public static final List<Breadcrumb> DELIVERY_BREADCRUMB = Arrays.asList(
			new Breadcrumb(false, "/shop", "Каталог"),
			new Breadcrumb(false, "/shop/checkout", "Контактная информация"),
			new Breadcrumb(true, null, "Способ доставки"),
			new Breadcrumb(false, "/shop/checkout/payment", "Оплата")
	);

	public static final List<Breadcrumb> PAYMENT_BREADCRUMB = Arrays.asList(
			new Breadcrumb(false, "/shop", "Каталог"),
			new Breadcrumb(false, "/shop/checkout", "Контактная информация"),
			new Breadcrumb(false, "/shop/checkout/delivery", "Способ доставки"),
			new Breadcrumb(true, null, "Оплата")
	);
}
