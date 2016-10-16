package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;

import java.util.Date;


@Deployment(table = "orders")
public class Order extends Cart
{
	public String orderNumber;
	public Date orderDate;

	@Override
	public String getFullText()
	{
		return orderNumber + name + phone;
	}

}
