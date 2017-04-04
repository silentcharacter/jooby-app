package com.mycompany.service.shop;

import com.mycompany.domain.shop.DeliveryType;
import com.mycompany.service.AbstractService;


public class DeliveryTypeService extends AbstractService<DeliveryType> {

	public DeliveryTypeService()
	{
		super(DeliveryType.class);
	}

	public boolean isFree(String deliveryTypeId) {
		return DeliveryType.FREE.equals(getById(deliveryTypeId).name);
	}

}
