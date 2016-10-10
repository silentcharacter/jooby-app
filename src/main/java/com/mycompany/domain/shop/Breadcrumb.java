package com.mycompany.domain.shop;

public class Breadcrumb
{

	public Breadcrumb(boolean current, String link, String name)
	{
		this.current = current;
		this.link = link;
		this.name = name;
	}

	public boolean current;
	public String link;
	public String name;
}
