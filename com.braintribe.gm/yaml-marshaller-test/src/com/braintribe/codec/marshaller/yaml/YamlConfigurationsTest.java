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
package com.braintribe.codec.marshaller.yaml;

import java.io.StringReader;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.codec.marshaller.yaml.model.SimpleEntity;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.ParseError;
import com.braintribe.model.generic.reflection.Property;

public class YamlConfigurationsTest {
	@Test
	public void testAbsenceInformation() {
		String yaml = "{}";
		
		Maybe<SimpleEntity> maybe = YamlConfigurations.read(SimpleEntity.T) //
			.absentifyMissingProperties() //
			.from(new StringReader(yaml));
		
		SimpleEntity simpleEntity = maybe.get();
		
		Property property = SimpleEntity.T.getProperty("string");
		
		Assertions.assertThat(property.isAbsent(simpleEntity)).isTrue();
	}
	
	@Test
	public void test1stOrderParseError() {
		String yaml = "{:}";
		
		Maybe<SimpleEntity> maybe = YamlConfigurations.read(SimpleEntity.T) //
				.from(new StringReader(yaml));

		Assertions.assertThat(maybe.isUnsatisfiedBy(ParseError.T)).isTrue();
		
		System.out.println(maybe.whyUnsatisfied().stringify());
		
	}
}
