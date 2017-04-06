package com.mycompany;

import com.mycompany.util.Utils;


/**
 * Created by Ilya on 13.12.2016.
 */
public class Test
{
	@org.junit.Test
	public void testRegexp()
	{
		String s = "8(906) 123 - 23-32f";
		System.out.println(Utils.formatPhone(s));
	}

	@org.junit.Test
	public void test() throws Exception
	{
		Double d1 = 198d;
		Double d2 = 17.33d;
		System.out.println(d1+ d2);

	}
}
