package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;


@Deployment(table = "orders")
public class Order extends Cart
{
	public String orderNumber;

	@Override
	public String getFullText()
	{
		return orderNumber + name + phone;
	}

}
