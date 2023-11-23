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
package com.braintribe.model.processing.generic.itw;

import static com.braintribe.model.processing.itw.InitializerTools.parseInitializer;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.tools.GmValueCodec.EnumParsingMode;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.NullDescriptor;
import com.braintribe.model.processing.itw.InitializerTools;
import com.braintribe.model.processing.itw.InitializerTools.EnumHint;

public class InitializerToolsTest {

	private static final EnumHint COLOR_HINT = new EnumHint(Color.class.getName(), asSet("red", "green", "blue"));
	private static final EnumHint[] colorValueHints = new EnumHint[] { null, COLOR_HINT };

	@Test
	public void parseSimpleTypes() {
		assertThat(parse("'hello'")).isEqualTo("hello");
		assertThat(parse("'hell'o'")).isEqualTo("hell'o");
		assertThat(parse("'hell,o'")).isEqualTo("hell,o");
		assertThat(parse("1")).isEqualTo(1);
		assertThat(parse("-1")).isEqualTo(-1);
		assertThat(parse("5L")).isEqualTo(5L);
		assertThat(parse("5f")).isEqualTo(5f);
		assertThat(parse("5d")).isEqualTo(5d);
		assertThat(parse("5b")).isEqualTo(new BigDecimal("5"));
		assertThat(parse("true")).isEqualTo(true);
		assertThat(parse("false")).isEqualTo(false);
		assertThat(parse("GMT:0")).isEqualTo(new Date(0));
	}

	@Test
	public void parseNull() {
		assertThat(parse("null")).isInstanceOf(NullDescriptor.class);
	}

	@Test
	public void parseEnum() {
		assertThat(parse("enum(com.braintribe.model.processing.generic.itw.Color,green)")).isEqualTo(Color.green);
		assertThat(parse("enum(Color,green)", EnumParsingMode.enumAsStringArray)).isEqualTo(new String[] { "Color", "green" });
		assertThat(parseInitializer("green", EnumParsingMode.enumAsValue, colorValueHints)).isEqualTo(Color.green);

		EnumReference enumReference = (EnumReference) parse("enum(Color,green)", EnumParsingMode.enumAsReference);
		assertThat(enumReference.getTypeSignature()).isEqualTo("Color");
		assertThat(enumReference.getConstant()).isEqualTo("green");
	}

	@Test
	public void parseMalformedEnum() {
		try {
			parse("enum(green)");
			throw new RuntimeException("Parsing should have failed.");
		} catch (IllegalArgumentException ignored) {
			// desired
		}

		try {
			parse("enum(green)", EnumParsingMode.enumAsStringArray);
			throw new RuntimeException("Parsing should have failed.");
		} catch (IllegalArgumentException ignored) {
			// desired
		}

		try {
			parse("enum(green)", EnumParsingMode.enumAsReference);
			throw new RuntimeException("Parsing should have failed.");
		} catch (IllegalArgumentException ignored) {
			// desired
		}
	}

	@Test
	public void parseNow() {
		assertThat(parse("now()")).isInstanceOf(Now.class);
	}

	@Test
	public void parseUuid() {
		Object parsed = parse("uuid()");
		assertThat(parsed).isNotNull();
	}

	@Test
	public void parseList() {
		assertThat(parse("['hello','world']")).isEqualTo(asList("hello", "world"));
		assertThat(parse("['hello,, world']")).isEqualTo(asList("hello, world"));
		assertThat(parse("[1l,'ONE']")).isEqualTo(asList(1L, "ONE"));
	}

	@Test
	public void parseSet() {
		assertThat(parse("{'hello','world'}")).isEqualTo(asSet("hello", "world"));
	}

	@Test
	public void parseEnumSet() {
		Object actual = parse(
				"{enum(com.braintribe.model.processing.generic.itw.Color,,green),enum(com.braintribe.model.processing.generic.itw.Color,,red)}");
		assertThat(actual).isEqualTo(asSet(Color.green, Color.red));
	}

	@Test
	public void parseMap() {
		assertThat(parse("map['hello','world']")).isEqualTo(asMap("hello", "world"));
		assertThat(parse("map['hello,,','world']")).isEqualTo(asMap("hello,", "world"));
	}

	private Object parse(String s) {
		return parse(s, EnumParsingMode.enumAsValue);
	}

	private Object parse(String s, EnumParsingMode enumParsingMode) {
		return parseInitializer(s, enumParsingMode, null);
	}

	@Test
	public void parsingAndStringifying() throws Exception {
		runParsingAndStrigyfying("string");
		runParsingAndStrigyfying("null");
		runParsingAndStrigyfying(Boolean.FALSE);
		runParsingAndStrigyfying(Boolean.TRUE);
		runParsingAndStrigyfying(123);
		runParsingAndStrigyfying(123l);
		runParsingAndStrigyfying(new BigDecimal("12354"));
		runParsingAndStrigyfying(123f);
		runParsingAndStrigyfying(123.e30f);
		runParsingAndStrigyfying(123.e30d);
		runParsingAndStrigyfying(NullDescriptor.T.create());
		runParsingAndStrigyfying(Now.T.create());
		runParsingAndStrigyfying("string");
		runParsingAndStrigyfying(Color.green);
		runParsingAndStrigyfying(new Date());
		runParsingAndStrigyfying(asList(1, 2, "three"));
		runParsingAndStrigyfying(asSet(1, 2, "three"));
		runParsingAndStrigyfying(asMap(1, "one", 2, "two"));
		runParsingAndStrigyfying(asMap(1, "one,ONE", 2, "two,TWO"));
	}

	private void runParsingAndStrigyfying(Object value) {
		String s = InitializerTools.stringifyInitializer(value);
		Object parsedValue = InitializerTools.parseInitializer(s, EnumParsingMode.enumAsValue, null);

		String dsc = "Problem with parsing/stringifying. Value '" + value + "', stringified: '" + s + "', parsed: '" + parsedValue;

		if (value instanceof GenericEntity) {
			assertThat(value.getClass()).as(dsc).isSameAs(parsedValue.getClass());

		} else {
			assertThat(parsedValue).as(dsc).isEqualTo(value);
		}
	}

}
