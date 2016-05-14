package com.mycompany.domain.shop;

import com.mycompany.domain.Entity;


public class Order extends Entity
{
	public String orderNumber;
	public String name;
	public String phone;
	public String streetName;
	public String streetNumber;
	public String entrance;
	public String flat;
	public String delivery;
	public String deliveryDate;
	public String deliveryTime;
	public String payment;

	@Override
	public String getFullText()
	{
		return orderNumber + name + phone;
	}
}
