package com.mycompany.service.shop;

import com.google.inject.Inject;
import com.mycompany.domain.shop.Menu;
import com.mycompany.service.AbstractService;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;


public class MenuService extends AbstractService<Menu>
{
	@Inject
	private CategoryService categoryService;
	@Inject
	private CmsPageService cmsPageService;

	public MenuService()
	{
		super(Menu.class);
	}

	public List<Menu> getAll() {
		return getAll(0, Integer.MAX_VALUE, "{index: 1}", "{active: true}", Collections.emptyList());
	}

	@Override
	public void onSave(Menu object)
	{
		object.url = StringUtils.isNotEmpty(object.categoryId)?
				("/#" + categoryService.getById(object.categoryId).cmsId) : ("/cms" + cmsPageService.getById(object.cmsPageId).url);
	}
}
