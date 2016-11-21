package com.mycompany.controller.shop;

import com.mycompany.controller.AbstractResource;
import com.mycompany.domain.shop.GlobalConfig;


public class GlobalConfigs extends AbstractResource<GlobalConfig>
{
	public GlobalConfigs()
	{
		super(GlobalConfig.class);
	}

	{
		initializeRoutes();
	}
}
