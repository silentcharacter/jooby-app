package com.mycompany.domain.shop;

public class Order extends Cart
{
	public String orderNumber;

	@Override
	public String getFullText()
	{
		return orderNumber + name + phone;
	}

}
