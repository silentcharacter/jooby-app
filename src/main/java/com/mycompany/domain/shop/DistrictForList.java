package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.controller.shop.Point;
import com.mycompany.domain.Entity;

import java.util.List;


@Deployment(table = "districts")
public class DistrictForList extends Entity
{
	public String name;

	@Override
	public String getFullText()
	{
		return name;
	}

}
