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

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Test;

public class TeeReaderTest {

	@SuppressWarnings("resource")
	@Test
	public void testTeeReader() throws Exception {

		String originalString = "Hello, world";

		StringReader sr = new StringReader(originalString);

		TeeReader r = new TeeReader(sr);

		BufferedReader br = new BufferedReader(r);

		String result = br.readLine();

		assertThat(result).isEqualTo(originalString);

		assertThat(r.getBuffer()).isEqualTo(originalString);

	}

	@SuppressWarnings("resource")
	@Test
	public void testTeeReaderLimited() throws Exception {

		String originalString = "Hello, world";

		StringReader sr = new StringReader(originalString);

		TeeReader r = new TeeReader(sr, 10);

		BufferedReader br = new BufferedReader(r);

		String result = br.readLine();

		assertThat(result).isEqualTo(originalString);

		assertThat(r.getBuffer()).isEqualTo("llo, world");

	}
}
