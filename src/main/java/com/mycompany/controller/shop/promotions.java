package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Promotion;
import com.mycompany.service.shop.PromotionService;


public class promotions extends AbstractResource<Promotion>
{

	public promotions() {
		super(Promotion.class, PromotionService.class);
	}
}
