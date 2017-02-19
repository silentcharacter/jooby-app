package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Customer;


public class Customers extends AbstractResource<Customer>
{
	public Customers() {
		super(Customer.class);
	}

	{
		initializeRoutes();
	}
}
