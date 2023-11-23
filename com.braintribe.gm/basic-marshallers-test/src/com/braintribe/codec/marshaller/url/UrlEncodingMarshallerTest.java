// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.codec.marshaller.url;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.braintribe.codec.marshaller.api.DateFormatOption;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.marshaller.impl.basic.test.model.A;
import com.braintribe.marshaller.impl.basic.test.model.B;
import com.braintribe.marshaller.impl.basic.test.model.JackOfAllTrades;
import com.braintribe.marshaller.impl.basic.test.model.PrimitiveJohn;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

public class UrlEncodingMarshallerTest {

	@Test
	public void testUrlEncodedSimpleEntity() throws Exception {
		UrlEncodingMarshaller m = new UrlEncodingMarshaller();
		PrimitiveJohn e = PrimitiveJohn.T.create();
		e.setPrimitiveBooleanValue(true);
		e.setPrimitiveDoubleValue(100d);
		e.setPrimitiveFloatValue(200f);
		e.setPrimitiveIntegerValue(300);
		e.setPrimitiveLongValue(400l);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		m.marshall(out, e);

		assetResult(e, out.toString());
	}

	@Test
	public void testUrlEncodedComplexEntity() throws Exception {
		UrlEncodingMarshaller m = new UrlEncodingMarshaller();
		JackOfAllTrades e = JackOfAllTrades.T.create();
		e.setPrimitiveBooleanValue(true);
		e.setPrimitiveDoubleValue(100d);
		e.setPrimitiveFloatValue(200f);
		e.setPrimitiveIntegerValue(300);
		e.setPrimitiveLongValue(400l);
		e.setStringList(Arrays.asList("foo", "bar"));
		e.setDateValue(new Date());

		JackOfAllTrades other = JackOfAllTrades.T.create();
		other.setPrimitiveBooleanValue(false);
		other.setPrimitiveDoubleValue(101d);
		other.setPrimitiveFloatValue(201f);
		other.setPrimitiveIntegerValue(301);
		other.setPrimitiveLongValue(401l);

		e.setOther(other);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		m.marshall(out, e);

		assetResult(e, out.toString());
	}

	@Test
	public void testUrlEncodedEntityWithCustomDateFormat() throws Exception {
		UrlEncodingMarshaller m = new UrlEncodingMarshaller();
		JackOfAllTrades e = JackOfAllTrades.T.create();
		e.setPrimitiveBooleanValue(true);
		e.setPrimitiveDoubleValue(100d);
		e.setPrimitiveFloatValue(200f);
		e.setPrimitiveIntegerValue(300);
		e.setPrimitiveLongValue(400l);
		e.setDateValue(new Date());

		GmSerializationOptions options = GmSerializationOptions.deriveDefaults().set(DateFormatOption.class, "dd.MM.yyyy").build();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		m.marshall(out, e, options);

		assetResult(e, out.toString());
	}

	@Test
	public void testUrlEncodedComplexEntity2() throws Exception {
		UrlEncodingMarshaller m = new UrlEncodingMarshaller();

		A a = A.T.create();
		B b = B.T.create();

		a.setBoolean(true);
		a.setReference(b);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		m.marshall(out, a);

		assetResult(a, out.toString());
	}

	@Test
	public void testUrlEncodedMap() throws Exception {
		UrlEncodingMarshaller m = new UrlEncodingMarshaller();
		Map<String, Object> e = new HashMap<>();
		e.put("boolean", true);
		e.put("double", 100d);
		e.put("float", 200f);
		e.put("int", 300);
		e.put("long", 400l);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		m.marshall(out, e);

		assetResult(e, out.toString());
	}

	@Test
	public void testUrlEncodedList() throws Exception {
		UrlEncodingMarshaller m = new UrlEncodingMarshaller();
		List<String> e = new ArrayList<>();
		e.add("element1");
		e.add("element2");
		e.add("element3");
		e.add("element4");
		e.add("element5");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		m.marshall(out, e);

		assetResult(e, out.toString());
	}

	@Test
	public void testUrlEncodedString() throws Exception {
		UrlEncodingMarshaller m = new UrlEncodingMarshaller();

		String e = "foo";
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		m.marshall(out, e);

		assetResult(e, out.toString());

	}

	@Test
	public void testUrlEncodedBigDecimal() throws Exception {
		UrlEncodingMarshaller m = new UrlEncodingMarshaller();

		BigDecimal e = new BigDecimal("100000000000.99");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		m.marshall(out, e);

		assetResult(e, out.toString());
	}

	@Test
	public void testRoundTrip() {
		UrlEncodingMarshaller m = new UrlEncodingMarshaller();
		JackOfAllTrades e = JackOfAllTrades.T.create();
		e.setPrimitiveBooleanValue(true);
		e.setPrimitiveDoubleValue(100d);
		e.setPrimitiveFloatValue(200f);
		e.setPrimitiveIntegerValue(300);
		e.setPrimitiveLongValue(400l);
		e.setStringList(Arrays.asList("foo", "bar", "foo"));
		e.setDateValue(new Date());

		JackOfAllTrades other = JackOfAllTrades.T.create();
		other.setPrimitiveBooleanValue(false);
		other.setPrimitiveDoubleValue(101d);
		other.setPrimitiveFloatValue(201f);
		other.setPrimitiveIntegerValue(301);
		other.setPrimitiveLongValue(401l);

		e.setOther(other);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		m.marshall(out, e);

		String marshalledString = out.toString();

		assetResult(e, marshalledString);

		JackOfAllTrades unmarshalled = (JackOfAllTrades) m.unmarshall(marshalledString,
				GmDeserializationOptions.deriveDefaults().setInferredRootType(JackOfAllTrades.T).build());

		assertEqual(e, unmarshalled);
		assertEqual(e.getOther(), unmarshalled.getOther());
	}

	private void assertEqual(JackOfAllTrades e, JackOfAllTrades other) {
		for (Property p : e.entityType().getProperties()) {
			Object originalValue = p.get(e);
			if (p.getType().isSimple() || p.getName().equals("stringList")) {
				Assertions.assertThat(originalValue).isEqualTo(p.get(other));
			}
		}
	}

	private void assetResult(Object actualValue, String encodedValue) {
		System.out.println("Marshalled value: " + actualValue + " to: " + encodedValue);

		/* String[] elements = encodedValue.split("\\&"); List<String> actualKeys = extractKeys(actualValue); List<String>
		 * actualValues = extractValues(actualValue);
		 * 
		 * for (String element : elements) { String[] components = element.split("=");
		 * 
		 * if (components.length == 2) { String key = components[0]; String value = components[1];
		 * 
		 * assertThat("", key, Matcher); } } */
	}

}
