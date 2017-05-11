package com.mycompany.controller.shop;

public class Point
{
	public Double x,y;

	public Point()
	{
	}

	public Point(String x, String y)
	{
		this.x = Double.valueOf(x);
		this.y = Double.valueOf(y);
	}
}
