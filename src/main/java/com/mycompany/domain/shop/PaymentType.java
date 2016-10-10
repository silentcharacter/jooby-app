package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;

@Deployment(table = "paymentTypes")
public class PaymentType extends Product {

	public static String OFFLINE = "offline";
}
