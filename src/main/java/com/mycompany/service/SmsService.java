package com.mycompany.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.mycompany.domain.shop.GlobalConfig;
import com.mycompany.domain.shop.Order;
import com.mycompany.service.shop.DeliveryTypeService;
import com.mycompany.service.shop.GlobalConfigService;
import com.mycompany.util.Utils;
import com.typesafe.config.Config;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.net.URLEncoder;


public class SmsService
{
	private final static Logger logger = LoggerFactory.getLogger(SmsService.class);
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

	@Inject
	private Config config;
	@Inject
	private GlobalConfigService globalConfigService;
	@Inject
	private DeliveryTypeService deliveryTypeService;

	public boolean sendOrderConfirmationSms(Order order, boolean createdByAdmin) throws UnsupportedEncodingException
	{
		GlobalConfig globalConfig = globalConfigService.getAll().get(0);

		if (!BooleanUtils.isTrue(globalConfig.sendSms)) {
			return false;
		}

		String text;
		if (!createdByAdmin) {
			text = String.format(globalConfig.smsTemplate, order.orderNumber);
		} else {
			String deliveryTime = deliveryTypeService.isFree(order.deliveryId)? "" : " " + order.deliveryTime;
			text = String.format(globalConfig.smsTemplateAdmin, order.orderNumber, dateFormat.format(order.deliveryDate), deliveryTime);
		}
		String phone = Utils.formatPhone(order.phone).replace("+7", "8");
		String url = String.format("https://gate.smsaero.ru/send/?user=%s&password=%s&text=%s&digital=0&answer=json&from=SUN+FOOD&to=%s",
				URLEncoder.encode(config.getString("smsaero.user"), "utf-8"), config.getString("smsaero.password"),
				URLEncoder.encode(text, "utf-8"), phone);
		HttpClient client = HttpClientBuilder.create().build();
		try
		{
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			String strResponse = EntityUtils.toString(entity);
			ObjectMapper mapper = new ObjectMapper();
			Map map = mapper.readValue(strResponse, Map.class);
			boolean res = "accepted".equals(map.get("result"));
			if (!res) {
				logger.error("Error sending sms: " + map.get("reason"));
			}
			return res;
		}
		catch (IOException e)
		{
			logger.error("Error sending sms", e);
		}
		return false;
	}
}
