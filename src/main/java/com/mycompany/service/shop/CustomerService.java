package com.mycompany.service.shop;

import com.mycompany.domain.shop.Customer;
import com.mycompany.service.AbstractService;
import com.mycompany.util.Utils;


public class CustomerService extends AbstractService<Customer> {

	public CustomerService()
	{
		super(Customer.class);
	}

	@Override
	public void onSave(Customer customer)
	{
		customer.phone = Utils.formatPhone(customer.phone);
	}
}
