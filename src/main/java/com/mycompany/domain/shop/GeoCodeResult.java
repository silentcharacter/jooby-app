package com.mycompany.domain.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;



/**
 * Created by Ilya on 29.12.2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoCodeResult
{
	@JsonProperty("geometry")
	public Geometry geometry;
}
