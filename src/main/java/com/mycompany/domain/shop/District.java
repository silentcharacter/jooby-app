package com.mycompany.domain.shop;

import com.mycompany.annotation.Deployment;
import com.mycompany.controller.shop.Point;

import java.util.List;

@Deployment(table = "districts")
public class District extends DistrictForList
{
	public List<Point> points;

}
