package com.mycompany.service.shop;

import com.mycompany.domain.shop.Tag;
import com.mycompany.service.AbstractService;


public class TagService extends AbstractService<Tag> {

	private Object popular;

	public TagService() {
        super(Tag.class);
    }

	public Tag getPopular()
	{
		return getBy("name", "Популярный");
	}

	public Tag getNew()
	{
		return getBy("name", "Новинки");
	}
}
