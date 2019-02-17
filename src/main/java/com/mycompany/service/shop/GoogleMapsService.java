package com.mycompany.service.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.mycompany.controller.shop.Point;
import com.mycompany.domain.shop.District;
import com.mycompany.domain.shop.DistrictForList;
import com.mycompany.domain.shop.GeoCodeResults;
import com.mycompany.domain.shop.Geometry;
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
import java.util.Optional;


public class GoogleMapsService
{
	private final static Logger logger = LoggerFactory.getLogger(GoogleMapsService.class);

	@Inject
	private DistrictService districtService;
	@Inject
	private Config config;

	public Geometry getCoordinates(String streetName, Integer streetNumber, String litera, Integer korpus) {
		StringBuilder streetNumberSb = new StringBuilder(Optional.ofNullable(streetNumber).orElse(0).toString());
		if (litera != null) {
			streetNumberSb.append(litera);
		}
		if (korpus != null) {
			streetNumberSb.append("к").append(korpus.toString());
		}
		return getCoordinates(streetName, streetNumberSb.toString());
	}

	public Geometry getCoordinates(String streetName, String streetNumber) {
		StringBuilder url = new StringBuilder("https://maps.google.com/maps/api/geocode/json?address=Россия+Ярославль+");
		url.append(streetName.trim().replaceAll("\\s", "+")).append("+");
		url.append(streetNumber.trim().replaceAll("\\s", "+"));
		url.append("&key=").append(config.getString("google.map.key"));
		Geometry res;
		try
		{
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url.toString());
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			String strResponse = EntityUtils.toString(entity);
			ObjectMapper mapper = new ObjectMapper();
			res = mapper.readValue(strResponse, GeoCodeResults.class).getGeometry();
		}
		catch (IOException e)
		{
			logger.error("Error getting coordinates", e);
			return null;
		}
		return res;
	}

	public District defineDistrict(Geometry geometry) {
		return districtService.getAll().stream().map(d -> districtService.getById(d.id)).filter(d -> {
			boolean c = false;
			Point p = new Point(geometry.getLat(), geometry.getLng());
			for (int i = 0, j = d.points.size() - 1; i < d.points.size(); j = i++) {
				if (((d.points.get(i).y < d.points.get(j).y) && (d.points.get(i).y <= p.y) && (p.y <= d.points.get(j).y) &&
						((d.points.get(j).y - d.points.get(i).y) * (p.x - d.points.get(i).x) > (d.points.get(j).x - d.points.get(i).x) * (p.y - d.points.get(i).y))
				) || ((d.points.get(i).y > d.points.get(j).y) && (d.points.get(j).y <= p.y) && (p.y <= d.points.get(i).y) &&
						((d.points.get(j).y - d.points.get(i).y) * (p.x - d.points.get(i).x) < (d.points.get(j).x - d.points.get(i).x) * (p.y - d.points.get(i).y)))) {
					c = !c;
				}
			}
			return c;
		}).findAny().orElse(null);
	}
}
