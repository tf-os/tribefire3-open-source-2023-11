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
package com.braintribe.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class LimitedStringBuilderTest {

	@Test
	public void testLimitedStringBuilder() throws Exception {

		LimitedStringBuilder b = new LimitedStringBuilder(10);
		b.append("Hello, world");
		assertThat(b.toString()).isEqualTo("llo, world");

		b = new LimitedStringBuilder(12);
		b.append("Hello, world");
		assertThat(b.toString()).isEqualTo("Hello, world");

		b = new LimitedStringBuilder(15);
		b.append("Hello, world");
		assertThat(b.toString()).isEqualTo("Hello, world");

		b = new LimitedStringBuilder(1);
		b.append("Hello, world");
		assertThat(b.toString()).isEqualTo("d");

		b = new LimitedStringBuilder(10);
		b.append("Hello, ");
		b.append("world");
		assertThat(b.toString()).isEqualTo("llo, world");

		b = new LimitedStringBuilder(10);
		b.append("Hello, ");
		b.append("world 1234567890");
		assertThat(b.toString()).isEqualTo("1234567890");

		b = new LimitedStringBuilder(10);
		b.append("Hello, ");
		b.append("world 1234567890");
		b.append('a');
		assertThat(b.toString()).isEqualTo("234567890a");

		b = new LimitedStringBuilder(Integer.MAX_VALUE);
		b.append("Hello, world");
		assertThat(b.toString()).isEqualTo("Hello, world");

	}
}
