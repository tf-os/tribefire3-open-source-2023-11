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
package com.braintribe.gm.yaml.config.test;

import java.io.File;
import java.io.StringReader;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.gm.config.yaml.ModeledYamlConfiguration;
import com.braintribe.gm.config.yaml.YamlConfigurations;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.config.ConfigurationError;
import com.braintribe.gm.model.reason.essential.ParseError;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;

public class ModeledYamlConfigTest {
	@Test
	public void testAbsenceInformation() {
		String yaml = "{}";
		
		Maybe<SimpleEntity> maybe = YamlConfigurations.read(SimpleEntity.T) //
			.absentifyMissingProperties() //
			.from(new StringReader(yaml));
		
		SimpleEntity simpleEntity = maybe.get();
		
		Property property = SimpleEntity.T.getProperty("stringProperty");
		
		Assertions.assertThat(property.isAbsent(simpleEntity)).isTrue();
	}
	
	@Test
	public void test1stOrderParseError() {
		
		Maybe<SimpleEntity> maybe = YamlConfigurations.read(SimpleEntity.T) //
				.from(new File("res/syntax-error.yaml"));

		Assertions.assertThat(maybe.isUnsatisfiedBy(ConfigurationError.T)).isTrue();
		
		System.out.println(maybe.whyUnsatisfied().stringify());
		
	}
	
	@Test
	public void test2ndOrderParseError() {
		Maybe<SimpleEntity> maybe = YamlConfigurations.read(SimpleEntity.T) //
				.from(new File("res/property-error.yaml"));

		Assertions.assertThat(maybe.isUnsatisfiedBy(ConfigurationError.T)).isTrue();
		
		System.out.println(maybe.whyUnsatisfied().stringify());
			
	}

	@Test
	public void testPropertyInjection() {
		ModeledYamlConfiguration config = new ModeledYamlConfiguration();
		config.setConfigFolder(new File("res/conf"));;
		SimpleEntity simpleEntity = config.configReasoned(SimpleEntity.T).get();
		Assertions.assertThat(simpleEntity.getStringProperty()).isEqualTo("first-second");
	}
}