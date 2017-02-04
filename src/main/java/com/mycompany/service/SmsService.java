package com.mycompany.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.mycompany.domain.shop.GeoCodeResults;
import com.mycompany.domain.shop.GlobalConfig;
import com.mycompany.domain.shop.Order;
import com.mycompany.service.shop.GlobalConfigService;
import com.typesafe.config.Config;
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
import java.util.Map;
import java.net.URLEncoder;


public class SmsService
{
	private final static Logger logger = LoggerFactory.getLogger(SmsService.class);

	@Inject
	private Config config;
	@Inject
	private GlobalConfigService globalConfigService;

	public boolean sendOrderConfirmationSms(Order order) throws UnsupportedEncodingException
	{
		GlobalConfig globalConfig = globalConfigService.getAll().get(0);

		String text = String.format(globalConfig.smsTemplate, order.orderNumber);
		String phone = order.phone.replace("+7", "8");
		phone = phone.length() == 10? "8" + phone : phone;
		String url = String.format("https://gate.smsaero.ru/send/?user=%s&password=%s&text=%s&digital=1&answer=json&from=NEWS&to=%s",
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
