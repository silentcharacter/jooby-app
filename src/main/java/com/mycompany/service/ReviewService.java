package com.mycompany.service;

import com.mycompany.domain.shop.Review;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.List;


public class ReviewService extends AbstractService<Review>
{
	public ReviewService()
	{
		super(Review.class);
	}

	@Override
	public void onSave(Review review) {
		if (review.image == null && StringUtils.isNotEmpty(review.id)) {
			review.image = getReviewImage(review.id);
		}
	}

	public Binary getReviewImage(String reviewId) {
		if (StringUtils.isEmpty(reviewId))
			return null;
		return getCollection().findOne(new ObjectId(reviewId)).as(Review.class).image;
	}

	public List<Review> getAll(Integer page, Integer perPage, String sort, String query, List<Object> filterValues) {
		List<Review> reviews = super.getAll(page, perPage, sort, query, filterValues);
		reviews.forEach(r -> r.text = r.text.replace("<p>", "").replace("</p>", ""));
		return reviews;
	}
}
