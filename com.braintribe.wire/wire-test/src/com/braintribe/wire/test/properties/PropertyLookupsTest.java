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
package com.braintribe.wire.test.properties;

import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;

import java.io.File;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.wire.impl.properties.PropertyLookups;

public class PropertyLookupsTest {

	private static final String ENCODED_DATE = "1992-10-10";

	@Test
	public void testLookups() {
		Map<String, String> properties = map(
				entry("SOME_NUMBER", "23"),
				entry("SOME_BOOLEAN", "true"),
				entry("A_FILE", "text.txt"),
				entry("MANDATORY_PROPERTY2", "Hello World!"),
				entry("RENAMED_VAR", "yeah"),
				entry("FILES", "./foo.txt,1%2C2,drei"),
				entry("NUMBERS", "23,42,815"),
				entry("NUMBER_TEXTS", "1=one,2=two,3=three"),
				entry("CONNECTION_PASSWORD", "peqwohYhZnSdwmjjHQygKzWorEUWsEmegMybHfNf5Qnd7wUL0Tl6p/nZbkiNx0W3o4LCDw=="),
				entry("A_DATE", ENCODED_DATE),
				entry("A_DURATION", "PT3.5S"),
				entry("ENUM", "one")
		);
		
		DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd")
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
	            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
	            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
	            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
	            .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
	            .toFormatter();
		
		ZonedDateTime referenceDateTime = ZonedDateTime.parse(ENCODED_DATE, dateFormatter);
		Date referenceDate =  Date.from(referenceDateTime.toInstant());
		
		ExampleProperties exampleProperties = PropertyLookups.create(ExampleProperties.class, properties::get);
		
		Assertions.assertThat(exampleProperties.SOME_NUMBER()).isEqualTo(23);
		Assertions.assertThat(exampleProperties.SOME_BOOLEAN()).isEqualTo(true);
		Assertions.assertThat(exampleProperties.DEFAULTED_BOOLEAN()).isEqualTo(true);
		Assertions.assertThat(exampleProperties.A_FILE(new File("."))).isEqualTo(new File("text.txt"));
		Assertions.assertThat(exampleProperties.ANOTHER_FILE(new File("."))).isEqualTo(new File("."));
		Assertions.assertThat(exampleProperties.MANDATORY_PROPERTY2()).isEqualTo("Hello World!");
		Assertions.assertThat(exampleProperties.renamed()).isEqualTo("yeah");
		Assertions.assertThat(exampleProperties.FILES()).isEqualTo(new ArrayList<>(Arrays.asList(new File("./foo.txt"), new File("1,2"), new File("drei"))));
		Assertions.assertThat(exampleProperties.NUMBERS()).isEqualTo(new HashSet<>(Arrays.asList(23,42,815)));
		Assertions.assertThat(exampleProperties.NUMBER_TEXTS()).isEqualTo(map(entry(1, "one"), entry(2, "two"), entry(3, "three")));
		Assertions.assertThat(exampleProperties.CONNECTION_PASSWORD()).isEqualTo("cortex");
		Assertions.assertThat(exampleProperties.CONNECTION_PASSWORD2()).isEqualTo("cortex");
		Assertions.assertThat(exampleProperties.CONNECTION_PASSWORD2("zVIpu/OIBKLU52n9psTVCz7mY6ehJ3V0yUlswNSDQNsay8Gzr4U9q69MtKUkSVzBjupbRg==")).isEqualTo("cortex");
		Assertions.assertThat(exampleProperties.A_DATE()).isEqualTo(referenceDate);
		Assertions.assertThat(exampleProperties.A_DURATION()).isEqualTo(Duration.parse("PT3.5S"));
		Assertions.assertThat(exampleProperties.ENUM()).isEqualTo(ExampleEnum.one);
		
		try {
			exampleProperties.INVALID_DEFAULTING("foobar");
			Assertions.fail("missing defaulting validation exception");
		}
		catch (IllegalStateException e) {
			// noop
		}
		
		try {
			exampleProperties.MANDATORY_PROPERTY1();
			Assertions.fail("missing mandatory exception");
		}
		catch (IllegalStateException e) {
			// noop
		}
		
		try {
			exampleProperties.MISSING_BOOLEAN();
			Assertions.fail("missing default parameter type mismatch exception");
		}
		catch (IllegalStateException e) {
			// noop
		}
		
		try {
			exampleProperties.BROKEN_PARAMETERIZED_BOOLEAN(true);
			Assertions.fail("missing exception");
		}
		catch (IllegalStateException e) {
			// noop
		}
	}
}
