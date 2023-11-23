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
package com.braintribe.utils.lcd;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Provides tests for {@link CommonTools}.
 *
 * @author michael.lafite
 */
public class CommonToolsTest {

	@Test
	public void testGetParametersString() {
		List<Object> parametersAndValues = Arrays.asList("a", 1, "b", 2, "c", 3, "f", 4, "e", 5, "d", 6);
		assertThat(CommonTools.getParametersString(parametersAndValues.toArray())).isEqualTo("(a=1,b=2,c=3,f=4,e=5,d=6)");

		// LinkedHashMap has insertion order
		Map<String, Integer> map = new LinkedHashMap<>();
		MapTools.putAll(map, parametersAndValues);
		assertThat(CommonTools.getParametersString(map)).isEqualTo("(a=1,b=2,c=3,f=4,e=5,d=6)");
	}
}
