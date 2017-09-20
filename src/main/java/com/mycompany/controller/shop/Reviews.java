package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Review;
import com.mycompany.service.ReviewService;


public class Reviews extends AbstractResource<Review>
{
	public Reviews() {
		super(Review.class, ReviewService.class);
	}
}
