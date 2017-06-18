package com.mycompany.service;

import org.jongo.MongoCursor;


public class SearchResult<T>
{
	public long count;
	public MongoCursor<Object> result;
}
