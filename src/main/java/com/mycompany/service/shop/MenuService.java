package com.mycompany.service.shop;

import com.mycompany.domain.shop.Menu;
import com.mycompany.service.AbstractService;

import java.util.Collections;
import java.util.List;


public class MenuService extends AbstractService<Menu>
{
	public MenuService()
	{
		super(Menu.class);
	}

	public List<Menu> getAll() {
		return getAll(0, Integer.MAX_VALUE, "{index: 1}", "{}", Collections.emptyList());
	}
}
