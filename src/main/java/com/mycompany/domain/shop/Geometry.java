package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Geometry
{
	@JsonProperty("location")
	public Map<String, String> location;

	public String getLat() {
		return location.get("lat");
	}

	public String getLng() {
		return location.get("lng");
	}
}
