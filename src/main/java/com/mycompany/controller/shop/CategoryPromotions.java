package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.CategoryPromotion;
import com.mycompany.service.shop.CategoryPromotionService;


public class CategoryPromotions extends AbstractResource<CategoryPromotion>
{

	public CategoryPromotions() {
		super(CategoryPromotion.class, CategoryPromotionService.class);
	}
}
