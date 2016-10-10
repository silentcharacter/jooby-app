package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;


@Deployment(table = "deliveryTypes")
public class DeliveryType extends Product {

	public static String FREE = "free";

}
