package com.mycompany.service.shop;

import com.google.inject.Inject;
import com.mycompany.domain.shop.Category;
import com.mycompany.domain.shop.CategoryPromotion;
import com.mycompany.service.AbstractService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;


public class CategoryPromotionService extends AbstractService<CategoryPromotion> {

	@Inject
	private CategoryService categoryService;

	public CategoryPromotionService() {
		super(CategoryPromotion.class);
	}

	@Override
	public void onSave(CategoryPromotion categoryPromotion) {
		if (StringUtils.isNotEmpty(categoryPromotion.categoryId)) {
			Category category = categoryService.getById(categoryPromotion.categoryId);
			if (category != null) {
				categoryPromotion.categoryCmsId = category.cmsId;
			}
		}
	}

	public CategoryPromotion findApplied(Map<String, Object> cart) {
		List<Map> entries = (List) cart.get("entries");
		if (entries == null) {
			return null;
		}
		for (Map entry : entries) {
			String categoryId = (String) ((Map)entry.get("product")).get("categoryId");
			if (categoryId != null) {
				List<CategoryPromotion> matching = getAll(String.format("{categoryId: '%s', active: true}", categoryId));
				if (matching != null && matching.size() > 0) {
					return matching.get(0);
				}
			}
		}
		return null;
	}
}
