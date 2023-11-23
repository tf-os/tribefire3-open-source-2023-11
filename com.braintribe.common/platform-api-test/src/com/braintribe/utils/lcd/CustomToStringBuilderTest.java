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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.utils.lcd.CustomToStringBuilder.CustomStringRepresentationProvider;

/**
 * Tests for {@link CustomToStringBuilder}.
 *
 * @author michael.lafite
 */

public class CustomToStringBuilderTest {

	@Test
	public void test() {

		final CustomStringRepresentationProvider stringRepresentationProvider = (Object object) -> {
			if (object instanceof Integer) {
				return "Int(" + object + ")";
			}
			return null;
		};

		final CustomToStringBuilder builder = new CustomToStringBuilder(stringRepresentationProvider);

		Assert.assertEquals("null", builder.toString("null"));
		Assert.assertEquals("xy", builder.toString("xy"));
		Assert.assertEquals("null", builder.toString(null));
		// elements sorted by string representation
		Assert.assertEquals("[Int(1), null, xy]", builder.toString(CommonTools.getSet("xy", (Object) null, 1)));

		Assert.assertEquals("Int(123)", builder.toString(Integer.valueOf(123)));

		final Map<Long, Object> map = new HashMap<>();
		map.put(3l, "3");
		map.put(2l, null);
		map.put(1l, 1);
		Assert.assertEquals("{1=Int(1), 2=null, 3=3}", builder.toString(map));

		final CustomToStringBuilder enhancedBuilderThatSupportsArrays = new com.braintribe.utils.CustomToStringBuilder(stringRepresentationProvider);
		Assert.assertEquals("[Int(123), Int(456)]", enhancedBuilderThatSupportsArrays.toString(new Integer[] { 123, 456 }));
		Assert.assertEquals("[Int(123), Int(456)]", enhancedBuilderThatSupportsArrays.toStringVarArgs(Integer.valueOf(123), Integer.valueOf(456)));
		Assert.assertEquals("[Int(123), Int(456)]", enhancedBuilderThatSupportsArrays.toStringVarArgs(Integer.valueOf(123), Integer.valueOf(456)));
	}
}
