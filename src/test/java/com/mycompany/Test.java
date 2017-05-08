package com.mycompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.domain.shop.Cart;
import com.mycompany.domain.shop.Product;
import com.mycompany.domain.shop.Sauce;
import com.mycompany.util.Utils;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

	@org.junit.Test
	public void test2() throws Exception
	{
		String line = "адрес ул 8 Марта 23 б 45 имя илья";
		String[] split = line.split("адрес|имя");
		Pattern pattern = Pattern.compile("(.*?)(\\d+)([\\s,а,б,в,г,д,е,ж]+)(\\d+)");
		Matcher matcher = pattern.matcher(split[1]);
		while (matcher.find()) {
//			System.out.println("group 0: " + matcher.group(0));
			System.out.println("group 1: " + matcher.group(1));
			System.out.println("group 2: " + matcher.group(2));
			System.out.println("group 3: " + matcher.group(3));
			System.out.println("group 4: " + matcher.group(4));
		}

	}

	@org.junit.Test
	public void testCartSerialization() throws Exception
	{
		Cart cart = new Cart();
		cart.deliveryPrice = 12;
		Product product = new Product();
		product.name = "name";
		product.id = "213";
		product.price = 1;
		Sauce sauce = new Sauce();
		sauce.price = 1;
		sauce.name = "name";
		sauce.id = "21333";
		cart.addEntry(product, 1, null, Collections.singletonList(sauce));

		ObjectMapper mapper = new ObjectMapper();
		String cartJsonString = mapper.writeValueAsString(cart);
		System.out.println(cartJsonString);
		cart = mapper.readValue(cartJsonString, Cart.class);
		System.out.println(cart);
	}

}
