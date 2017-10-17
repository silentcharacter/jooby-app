package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.domain.Entity;

@Deployment(table = "globalConfigs")
public class GlobalConfig extends Entity
{
	public String name;
	public int deliveryGap;
	public int deliveryDaysRange;
	public String smsTemplate;
	public Boolean sendSms;
	public String smsTemplateAdmin;
	public Boolean testMode;

	@Override
	public String getFullText()
	{
		return "";
	}
}
