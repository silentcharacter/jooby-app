package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.Menu;
import com.mycompany.service.shop.MenuService;


public class Menus extends AbstractResource<Menu>
{
	public Menus() {
		super(Menu.class, MenuService.class);
	}
}
