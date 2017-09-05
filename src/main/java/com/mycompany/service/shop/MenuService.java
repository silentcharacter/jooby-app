package com.mycompany.service.shop;

import com.mycompany.constant.Constants;
import com.mycompany.domain.shop.Menu;
import com.mycompany.service.AbstractService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class MenuService extends AbstractService<Menu>
{
	public MenuService()
	{
		super(Menu.class);
	}

	public List<Menu> getAll() {
		return getAll(0, Integer.MAX_VALUE, "{index: 1}", "{}", Collections.emptyList()).stream().map(menu -> {
			menu.url = menu.url.startsWith("#") ? (Constants.SHOP_PATH + menu.url) : menu.url;
			return menu;
		}).collect(Collectors.toList());
	}
}
