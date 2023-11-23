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
package com.braintribe.utils.stream;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;

import org.junit.Test;

public class CountingWriterTest {

	@SuppressWarnings("cast")
	@Test
	public void testCountingOuputStream() throws Exception {

		StringWriter sw = new StringWriter();

		CountingWriter cpw = new CountingWriter(sw);
		assertThat(cpw.getCount()).isEqualTo(0L);

		cpw.resetCount();
		cpw.append('1');
		assertThat(cpw.getCount()).isEqualTo(1);

		cpw.resetCount();
		cpw.append("hello");
		assertThat(cpw.getCount()).isEqualTo(5);

		cpw.resetCount();
		cpw.append("hello", 0, 1);
		assertThat(cpw.getCount()).isEqualTo(1);

		cpw.resetCount();
		cpw.write(new char[] { '1', '2' });
		assertThat(cpw.getCount()).isEqualTo(2);

		cpw.resetCount();
		cpw.write((int) '1');
		assertThat(cpw.getCount()).isEqualTo(1);

		cpw.resetCount();
		cpw.write("hello");
		assertThat(cpw.getCount()).isEqualTo(5);

		cpw.resetCount();
		cpw.write(new char[] { '1', '2' }, 0, 1);
		assertThat(cpw.getCount()).isEqualTo(1);

		cpw.resetCount();
		cpw.write("hello2", 0, 1);
		assertThat(cpw.getCount()).isEqualTo(1);
	}
}
