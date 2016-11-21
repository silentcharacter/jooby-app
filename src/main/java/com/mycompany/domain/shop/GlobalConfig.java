package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;

@Deployment(table = "globalConfigs")
public class GlobalConfig extends Entity
{
	public int deliveryGap;
	public String name;

	@Override
	public String getFullText()
	{
		return "";
	}
}
