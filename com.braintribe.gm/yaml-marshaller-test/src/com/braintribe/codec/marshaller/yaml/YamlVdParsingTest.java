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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.braintribe.codec.marshaller.yaml.model.TestEntity;
import com.braintribe.gm.model.reason.Maybe;

public class YamlVdParsingTest {
	private static YamlMarshaller yamlMarshaller = new YamlMarshaller();
	
	@Test
	public void testPlaceholderConfiguration() {
		Map<String, Object> vars = new HashMap<>();
		
		vars.put("longValue", "5");
		vars.put("intValue", "23");
		vars.put("listIntValue1", "1");
		vars.put("listIntValue2", "3");
		
		Maybe<TestEntity> entityMaybe = YamlConfigurations.read(TestEntity.T).placeholders(v -> vars.get(v.getName())).from(new File("res/vd-test.yaml"));
		
		TestEntity resultEntity = entityMaybe.get();
		
		assertThat(resultEntity.getLongValue()).isEqualTo(5L);
		assertThat(resultEntity.getIntValue()).isEqualTo(23);
		assertThat(resultEntity.getStringValue()).isEqualTo("$escape-test");
		assertThat(resultEntity.getIntegerList()).isEqualTo(Arrays.asList(1, 2, 3));
	}
	
	@Test
	public void testPlaceholderAndAbsence() {
		Map<String, Object> vars = new HashMap<>();
		
		vars.put("longValue", "5");
		vars.put("intValue", "23");
		vars.put("listIntValue1", "1");
		vars.put("listIntValue2", "3");
		
		Maybe<TestEntity> entityMaybe = YamlConfigurations.read(TestEntity.T) //
				.absentifyMissingProperties() //
				.placeholders(v -> vars.get(v.getName())) //
				.from(new File("res/vd-test.yaml")); 
		
		TestEntity resultEntity = entityMaybe.get();
		
		assertThat(resultEntity.getLongValue()).isEqualTo(5L);
		assertThat(resultEntity.getIntValue()).isEqualTo(23);
		assertThat(resultEntity.getStringValue()).isEqualTo("$escape-test");
		assertThat(resultEntity.getIntegerList()).isEqualTo(Arrays.asList(1, 2, 3));
	}
}
