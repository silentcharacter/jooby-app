package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


/**
 * Created by Ilya on 29.12.2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoCodeResults
{
	@JsonProperty("results")
	public List<GeoCodeResult> results;

	public Geometry getGeometry() {
		return results.get(0).geometry;
	}
}
