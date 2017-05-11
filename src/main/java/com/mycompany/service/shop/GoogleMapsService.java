package com.mycompany.service.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.mycompany.controller.shop.Point;
import com.mycompany.domain.shop.District;
import com.mycompany.domain.shop.DistrictForList;
import com.mycompany.domain.shop.GeoCodeResults;
import com.mycompany.domain.shop.Geometry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class GoogleMapsService
{
	private final static Logger logger = LoggerFactory.getLogger(GoogleMapsService.class);

	@Inject
	private DistrictService districtService;

	public Geometry getCoordinates(String streetName, String streetNumber) {
		String url = String.format("http://maps.google.com/maps/api/geocode/json?address=Россия+Ярославль+%s+%s",
				streetName.trim().replaceAll(" ", "+"), streetNumber.trim());

		Geometry res = new Geometry();
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		try
		{
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
