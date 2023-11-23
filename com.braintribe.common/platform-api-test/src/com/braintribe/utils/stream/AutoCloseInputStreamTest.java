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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.junit.Test;

import com.braintribe.provider.Holder;
import com.braintribe.utils.lcd.IOTools;

public class AutoCloseInputStreamTest {

	@Test
	public void testInputStream() throws Exception {

		Holder<Boolean> closedHolder = new Holder<>();
		closedHolder.accept(Boolean.FALSE);

		byte[] bytes = new byte[1024];
		new Random().nextBytes(bytes);

		InputStream bais = new AutoCloseInputStream(new ByteArrayInputStream(bytes) {
			@Override
			public void close() throws IOException {
				super.close();
				closedHolder.accept(Boolean.TRUE);
			}
		});

		ByteArrayOutputStream devNull = new ByteArrayOutputStream();
		IOTools.pump(bais, devNull);

		assertThat(closedHolder.get().booleanValue()).isTrue();
	}
}
