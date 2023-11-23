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
package com.braintribe.model.processing.manipulation.marshaller;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.SysPrint.spOut;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.io.StringWriter;
import java.math.BigDecimal;

import org.junit.Test;

import com.braintribe.model.manipulation.parser.impl.model.Joat;
import com.braintribe.model.manipulation.parser.impl.model.SomeEnum;

/**
 * @author peter.gazdik
 */
public class ManMarshallerTest {

	private String marshalled;

	private static final boolean PRINT = false;

	@Test
	public void writeEmptyEntity() throws Exception {
		Joat joat = Joat.T.create();

		marshall(joat);

		assertContains("Joat = com.braintribe.model.manipulation.parser.impl.model.Joat");
		assertContains("$       = Joat()");
		assertContains(".booleanValue = false");
		assertContains(".doubleValue = 0.0D");
		assertContains(".floatValue = 0.0F");
		assertContains(".integerValue = 0");
		assertContains(".longValue = 0L");
	}

	@Test
	public void writeScalars() throws Exception {
		Joat joat = Joat.T.create();
		joat.setBooleanValue(true);
		joat.setDecimalValue(BigDecimal.TEN);
		joat.setDoubleValue(123456789.0123);
		joat.setEnumValue(SomeEnum.foxy);
		joat.setFloatValue(12.5f);
		joat.setIntegerValue(77);
		joat.setLongValue(123456789);
		joat.setStringValue("str");

		marshall(joat);

		assertContains(".booleanValue = true");
		assertContains(".decimalValue = 10B");
		assertContains(".doubleValue = 1.234567890123E8D");
		assertContains(".enumValue = SomeEnum::foxy");
		assertContains(".floatValue = 12.5F");
		assertContains(".integerValue = 77");
		assertContains(".longValue = 123456789L");
		assertContains(".stringValue = 'str'");
	}

	@Test
	public void writeSingleElementCollections() throws Exception {
		Joat joat = Joat.T.create();
		joat.setStringList(asList("one"));
		joat.setStringSet(asSet("two"));
		joat.setStringObjectMap(asMap("three", 3));

		marshall(joat);

		assertContains(".stringList = ['one']");
		assertContains(".stringSet = ('two')");
		assertContains(".stringObjectMap = {'three':3}");
	}

	@Test
	public void writeMultiElementCollections() throws Exception {
		Joat joat = Joat.T.create();
		joat.setStringList(asList("one", "two", "three"));
		joat.setStringSet(asSet("four", "five"));
		joat.setStringObjectMap(asMap("ten", 10, "twenty", 20));

		marshall(joat);

		assertContains(".stringList = [\n" + //
				" 'one',\n" + //
				" 'two',\n" + //
				" 'three'\n" + //
				" ]");
		assertContains(".stringSet = (\n" + //
				" 'four',\n" + //
				" 'five'\n" + //
				" )");
		assertContains(".stringObjectMap = {\n" + //
				" 'ten':10,\n" + //
				" 'twenty':20\n" + //
				"}");

	}

	private void marshall(Joat joat) {
		StringWriter sw = new StringWriter();

		new ManMarshaller().marshall(sw, joat);

		marshalled = sw.toString();

		if (PRINT)
			spOut(marshalled);
	}

	private void assertContains(String s) {
		assertThat(marshalled).contains(s);
	}

}
