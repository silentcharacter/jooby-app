package com.mycompany.service.shop;

import com.google.inject.Inject;
import com.mycompany.domain.shop.Category;
import com.mycompany.domain.shop.Promotion;
import com.mycompany.service.AbstractService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;


public class PromotionService extends AbstractService<Promotion> {

	@Inject
	private CategoryService categoryService;

	public PromotionService() {
		super(Promotion.class);
	}

	@Override
	public void onSave(Promotion categoryPromotion) {
		if (StringUtils.isNotEmpty(categoryPromotion.categoryId)) {
			Category category = categoryService.getById(categoryPromotion.categoryId);
			if (category != null) {
				categoryPromotion.categoryCmsId = category.cmsId;
			}
		}
	}

	public Promotion findApplied(Map<String, Object> cart) {
		List<Map> entries = (List) cart.get("entries");
		if (entries == null) {
			return null;
		}
		for (Map entry : entries) {
			String productId = (String) entry.get("productId");
			String categoryId = (String) ((Map)entry.get("product")).get("categoryId");
			if (categoryId != null) {
				List<Promotion> matching = getAll(String.format("{categoryId: '%s', active: true}", categoryId));
				if (matching != null && matching.size() > 0) {
					return matching.get(0);
				}
			}
			if (productId != null) {
				List<Promotion> matching = getAll(String.format("{productId: '%s', active: true}", productId));
				if (matching != null && matching.size() > 0) {
					return matching.get(0);
				}
			}
		}
		return null;
	}
}
