package com.mycompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.domain.shop.Cart;
import com.mycompany.domain.shop.Product;
import com.mycompany.domain.shop.Sauce;
import com.mycompany.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


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

	@org.junit.Test
	public void testPolygon() throws Exception
	{
		String s = "\n4871.3643 = sum of:\n  4477.642 = sum of:\n    2885.0947 = sum of:\n      1412.8351 = weight(tdscLongDescription_en_stringci_mv:retainer in 16204) [], result of:\n        1412.8351 = score(doc=16204,freq=1.0 = termFreq=1.0\n), product of:\n          200.0 = boost\n          7.064175 = idf(docFreq=20, docCount=23970)\n          1.0 = tfNorm, computed from:\n            1.0 = termFreq=1.0\n            1.2 = parameter k1\n            0.0 = parameter b (norms omitted for field)\n      904.3994 = weight(tdscLongDescription_en_stringci_mv:box in 16204) [], result of:\n        904.3994 = score(doc=16204,freq=1.0 = termFreq=1.0\n), product of:\n          200.0 = boost\n          4.521997 = idf(docFreq=260, docCount=23970)\n          1.0 = tfNorm, computed from:\n            1.0 = termFreq=1.0\n            1.2 = parameter k1\n            0.0 = parameter b (norms omitted for field)\n      567.8603 = weight(tdscLongDescription_en_stringci_mv:blue in 16204) [], result of:\n        567.8603 = score(doc=16204,freq=1.0 = termFreq=1.0\n), product of:\n          200.0 = boost\n          2.8393016 = idf(docFreq=1401, docCount=23970)\n          1.0 = tfNorm, computed from:\n            1.0 = termFreq=1.0\n            1.2 = parameter k1\n            0.0 = parameter b (norms omitted for field)\n    706.41754 = weight(tdscLongDescription_en_stringci_mv:retainer in 16204) [], result of:\n      706.41754 = score(doc=16204,freq=1.0 = termFreq=1.0\n), product of:\n        100.0 = boost\n        7.064175 = idf(docFreq=20, docCount=23970)\n        1.0 = tfNorm, computed from:\n          1.0 = termFreq=1.0\n          1.2 = parameter k1\n          0.0 = parameter b (norms omitted for field)\n    50.0 = tdscLongDescription_en_stringci_mv:retainer*, product of:\n      50.0 = boost\n      1.0 = queryNorm\n    452.1997 = weight(tdscLongDescription_en_stringci_mv:box in 16204) [], result of:\n      452.1997 = score(doc=16204,freq=1.0 = termFreq=1.0\n), product of:\n        100.0 = boost\n        4.521997 = idf(docFreq=260, docCount=23970)\n        1.0 = tfNorm, computed from:\n          1.0 = termFreq=1.0\n          1.2 = parameter k1\n          0.0 = parameter b (norms omitted for field)\n    50.0 = tdscLongDescription_en_stringci_mv:box*, product of:\n      50.0 = boost\n      1.0 = queryNorm\n    283.93015 = weight(tdscLongDescription_en_stringci_mv:blue in 16204) [], result of:\n      283.93015 = score(doc=16204,freq=1.0 = termFreq=1.0\n), product of:\n        100.0 = boost\n        2.8393016 = idf(docFreq=1401, docCount=23970)\n        1.0 = tfNorm, computed from:\n          1.0 = termFreq=1.0\n          1.2 = parameter k1\n          0.0 = parameter b (norms omitted for field)\n    50.0 = tdscLongDescription_en_stringci_mv:blue*, product of:\n      50.0 = boost\n      1.0 = queryNorm\n  393.722 = sum of:\n    245.81465 = sum of:\n      245.81465 = weight(featuresFullText_en_stringci_mv:blue in 16204) [], result of:\n        245.81465 = score(doc=16204,freq=1.0 = termFreq=1.0\n), product of:\n          100.0 = boost\n          2.4581466 = idf(docFreq=1669, docCount=19504)\n          1.0 = tfNorm, computed from:\n            1.0 = termFreq=1.0\n            1.2 = parameter k1\n            0.0 = parameter b (norms omitted for field)\n    122.907326 = weight(featuresFullText_en_stringci_mv:blue in 16204) [], result of:\n      122.907326 = score(doc=16204,freq=1.0 = termFreq=1.0\n), product of:\n        50.0 = boost\n        2.4581466 = idf(docFreq=1669, docCount=19504)\n        1.0 = tfNorm, computed from:\n          1.0 = termFreq=1.0\n          1.2 = parameter k1\n          0.0 = parameter b (norms omitted for field)\n    25.0 = featuresFullText_en_stringci_mv:blue*, product of:\n      25.0 = boost\n      1.0 = queryNorm\n";
		System.out.println(s);
	}

}
