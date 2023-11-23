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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import org.junit.Test;

public class CountingPrintWriterTest {

	@Test
	public void testCountingOuputStream() {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		CountingPrintWriter cpw = new CountingPrintWriter(pw);
		assertThat(cpw.getCount()).isEqualTo(0L);

		cpw.resetCount();
		cpw.print(true);
		assertThat(cpw.getCount()).isEqualTo(4);

		cpw.resetCount();
		cpw.print(false);
		assertThat(cpw.getCount()).isEqualTo(5);

		cpw.resetCount();
		cpw.print('1');
		assertThat(cpw.getCount()).isEqualTo(1);

		cpw.resetCount();
		cpw.print(new char[] { '1', '2' });
		assertThat(cpw.getCount()).isEqualTo(2);

		cpw.resetCount();
		cpw.print(1.0d);
		assertThat(cpw.getCount()).isEqualTo(3);

		cpw.resetCount();
		cpw.print(1.0f);
		assertThat(cpw.getCount()).isEqualTo(3);

		cpw.resetCount();
		cpw.print(1);
		assertThat(cpw.getCount()).isEqualTo(1);

		cpw.resetCount();
		cpw.print(1l);
		assertThat(cpw.getCount()).isEqualTo(1);

		cpw.resetCount();
		cpw.print((Object) "hello");
		assertThat(cpw.getCount()).isEqualTo(5);

		cpw.resetCount();
		cpw.print((Object) "hello\n");
		assertThat(cpw.getCount()).isEqualTo(6);

		cpw.resetCount();
		cpw.print("hello");
		assertThat(cpw.getCount()).isEqualTo(5);

		cpw.resetCount();
		cpw.printf("%s", "hello");
		assertThat(cpw.getCount()).isEqualTo(5);

		cpw.resetCount();
		cpw.printf(Locale.US, "%s", "hello");
		assertThat(cpw.getCount()).isEqualTo(5);

		cpw.resetCount();
		cpw.println();
		assertThat(cpw.getCount()).isEqualTo(1);

		cpw.resetCount();
		cpw.println(true);
		assertThat(cpw.getCount()).isEqualTo(5);

		cpw.resetCount();
		cpw.println(false);
		assertThat(cpw.getCount()).isEqualTo(6);

		cpw.resetCount();
		cpw.println('1');
		assertThat(cpw.getCount()).isEqualTo(2);

		cpw.resetCount();
		cpw.println(new char[] { '1', '2' });
		assertThat(cpw.getCount()).isEqualTo(3);

		cpw.resetCount();
		cpw.println(1.0d);
		assertThat(cpw.getCount()).isEqualTo(4);

		cpw.resetCount();
		cpw.println(1.0f);
		assertThat(cpw.getCount()).isEqualTo(4);

		cpw.resetCount();
		cpw.println(1);
		assertThat(cpw.getCount()).isEqualTo(2);

		cpw.resetCount();
		cpw.println(1l);
		assertThat(cpw.getCount()).isEqualTo(2);

		cpw.resetCount();
		cpw.println((Object) "hello");
		assertThat(cpw.getCount()).isEqualTo(6);

		cpw.resetCount();
		cpw.println("hello");
		assertThat(cpw.getCount()).isEqualTo(6);

	}
}
