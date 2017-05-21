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
		String s = "\n15658.831 = sum of:\n  3645.8635 = sum of:\n    2219.4756 = sum of:\n      114.021286 = weight(tdscLongDescription_text_en_mv:blue in 11684) [], result of:\n        114.021286 = score(doc=11684,freq=1.0 = termFreq=1.0\n), product of:\n          200.0 = boost\n          2.8237388 = idf(docFreq=720, docCount=12132)\n          0.20189773 = tfNorm, computed from:\n            1.0 = termFreq=1.0\n            1.2 = parameter k1\n            0.75 = parameter b\n            24.00849 = avgFieldLength\n            256.0 = fieldLength\n      1458.8279 = weight(tdscLongDescription_text_en_mv:retain in 11684) [], result of:\n        1458.8279 = score(doc=11684,freq=9.0 = termFreq=9.0\n), product of:\n          200.0 = boost\n          6.961337 = idf(docFreq=11, docCount=12132)\n          1.0478072 = tfNorm, computed from:\n            9.0 = termFreq=9.0\n            1.2 = parameter k1\n            0.75 = parameter b\n            24.00849 = avgFieldLength\n            256.0 = fieldLength\n      646.62646 = weight(tdscLongDescription_text_en_mv:box in 11684) [], result of:\n        646.62646 = score(doc=11684,freq=9.0 = termFreq=9.0\n), product of:\n          200.0 = boost\n          3.0856175 = idf(docFreq=554, docCount=12132)\n          1.0478072 = tfNorm, computed from:\n            9.0 = termFreq=9.0\n            1.2 = parameter k1\n            0.75 = parameter b\n            24.00849 = avgFieldLength\n            256.0 = fieldLength\n    57.010643 = weight(tdscLongDescription_text_en_mv:blue in 11684) [], result of:\n      57.010643 = score(doc=11684,freq=1.0 = termFreq=1.0\n), product of:\n        100.0 = boost\n        2.8237388 = idf(docFreq=720, docCount=12132)\n        0.20189773 = tfNorm, computed from:\n          1.0 = termFreq=1.0\n          1.2 = parameter k1\n          0.75 = parameter b\n          24.00849 = avgFieldLength\n          256.0 = fieldLength\n    50.0 = tdscLongDescription_text_en_mv:blue*, product of:\n      50.0 = boost\n      1.0 = queryNorm\n    14.252661 = sum of:\n      14.252661 = weight(tdscLongDescription_text_en_mv:blue in 11684) [], result of:\n        14.252661 = score(doc=11684,freq=1.0 = termFreq=1.0\n), product of:\n          25.0 = boost\n          2.8237388 = idf(docFreq=720, docCount=12132)\n          0.20189773 = tfNorm, computed from:\n            1.0 = termFreq=1.0\n            1.2 = parameter k1\n            0.75 = parameter b\n            24.00849 = avgFieldLength\n            256.0 = fieldLength\n    729.41394 = weight(tdscLongDescription_text_en_mv:retain in 11684) [], result of:\n      729.41394 = score(doc=11684,freq=9.0 = termFreq=9.0\n), product of:\n        100.0 = boost\n        6.961337 = idf(docFreq=11, docCount=12132)\n        1.0478072 = tfNorm, computed from:\n          9.0 = termFreq=9.0\n          1.2 = parameter k1\n          0.75 = parameter b\n          24.00849 = avgFieldLength\n          256.0 = fieldLength\n    121.56898 = weight(tdscLongDescription_text_en_mv:retain in 11684) [], result of:\n      121.56898 = score(doc=11684,freq=9.0 = termFreq=9.0\n), product of:\n        16.666666 = boost\n        6.961337 = idf(docFreq=11, docCount=12132)\n        1.0478072 = tfNorm, computed from:\n          9.0 = termFreq=9.0\n          1.2 = parameter k1\n          0.75 = parameter b\n          24.00849 = avgFieldLength\n          256.0 = fieldLength\n    323.31323 = weight(tdscLongDescription_text_en_mv:box in 11684) [], result of:\n      323.31323 = score(doc=11684,freq=9.0 = termFreq=9.0\n), product of:\n        100.0 = boost\n        3.0856175 = idf(docFreq=554, docCount=12132)\n        1.0478072 = tfNorm, computed from:\n          9.0 = termFreq=9.0\n          1.2 = parameter k1\n          0.75 = parameter b\n          24.00849 = avgFieldLength\n          256.0 = fieldLength\n    50.0 = tdscLongDescription_text_en_mv:box*, product of:\n      50.0 = boost\n      1.0 = queryNorm\n    80.82831 = sum of:\n      80.82831 = weight(tdscLongDescription_text_en_mv:box in 11684) [], result of:\n        80.82831 = score(doc=11684,freq=9.0 = termFreq=9.0\n), product of:\n          25.0 = boost\n          3.0856175 = idf(docFreq=554, docCount=12132)\n          1.0478072 = tfNorm, computed from:\n            9.0 = termFreq=9.0\n            1.2 = parameter k1\n            0.75 = parameter b\n            24.00849 = avgFieldLength\n            256.0 = fieldLength\n  12012.968 = sum of:\n    7621.433 = sum of:\n      3671.1895 = weight(tdscShortDescription_text_en_mv:retain in 11684) [], result of:\n        3671.1895 = score(doc=11684,freq=1.0 = termFreq=1.0\n), product of:\n          400.0 = boost\n          7.1902394 = idf(docFreq=9, docCount=12600)\n          1.276449 = tfNorm, computed from:\n            1.0 = termFreq=1.0\n            1.2 = parameter k1\n            0.75 = parameter b\n            11.101984 = avgFieldLength\n            5.2244897 = fieldLength\n      3950.2437 = weight(tdscShortDescription_text_en_mv:box in 11684) [], result of:\n        3950.2437 = score(doc=11684,freq=1.0 = termFreq=1.0\n), product of:\n          400.0 = boost\n          7.7367835 = idf(docFreq=5, docCount=12600)\n          1.276449 = tfNorm, computed from:\n            1.0 = termFreq=1.0\n            1.2 = parameter k1\n            0.75 = parameter b\n            11.101984 = avgFieldLength\n            5.2244897 = fieldLength\n    1835.5947 = weight(tdscShortDescription_text_en_mv:retain in 11684) [], result of:\n      1835.5947 = score(doc=11684,freq=1.0 = termFreq=1.0\n), product of:\n        200.0 = boost\n        7.1902394 = idf(docFreq=9, docCount=12600)\n        1.276449 = tfNorm, computed from:\n          1.0 = termFreq=1.0\n          1.2 = parameter k1\n          0.75 = parameter b\n          11.101984 = avgFieldLength\n          5.2244897 = fieldLength\n    305.93243 = weight(tdscShortDescription_text_en_mv:retain in 11684) [], result of:\n      305.93243 = score(doc=11684,freq=1.0 = termFreq=1.0\n), product of:\n        33.333332 = boost\n        7.1902394 = idf(docFreq=9, docCount=12600)\n        1.276449 = tfNorm, computed from:\n          1.0 = termFreq=1.0\n          1.2 = parameter k1\n          0.75 = parameter b\n          11.101984 = avgFieldLength\n          5.2244897 = fieldLength\n    1975.1218 = weight(tdscShortDescription_text_en_mv:box in 11684) [], result of:\n      1975.1218 = score(doc=11684,freq=1.0 = termFreq=1.0\n), product of:\n        200.0 = boost\n        7.7367835 = idf(docFreq=5, docCount=12600)\n        1.276449 = tfNorm, computed from:\n          1.0 = termFreq=1.0\n          1.2 = parameter k1\n          0.75 = parameter b\n          11.101984 = avgFieldLength\n          5.2244897 = fieldLength\n    100.0 = tdscShortDescription_text_en_mv:box*, product of:\n      100.0 = boost\n      1.0 = queryNorm\n    174.88535 = sum of:\n      174.88535 = weight(tdscShortDescription_text_en_mv:box in 11684) [], result of:\n        174.88535 = score(doc=11684,freq=1.0 = termFreq=1.0\n), product of:\n          50.0 = boost\n          2.7401855 = idf(docFreq=813, docCount=12600)\n          1.276449 = tfNorm, computed from:\n            1.0 = termFreq=1.0\n            1.2 = parameter k1\n            0.75 = parameter b\n            11.101984 = avgFieldLength\n            5.2244897 = fieldLength\n";
		System.out.println(s);
	}

}
