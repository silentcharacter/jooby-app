package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Customer;
import com.mycompany.service.shop.CustomerService;


public class Customers extends AbstractResource<Customer>
{
	public Customers() {
		super(Customer.class, CustomerService.class);
	}
}
