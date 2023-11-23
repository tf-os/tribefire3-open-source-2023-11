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
import java.io.UnsupportedEncodingException;
import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.IOTools;

public class HardLimitInputStreamTest {

	@Test
	public void testLimits() throws Exception {

		Supplier<ByteArrayInputStream> inSupplier = () -> {
			try {
				return new ByteArrayInputStream("hello, world".getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw Exceptions.unchecked(e, "Well, this is unexpected.");
			}
		};

		try (HardLimitInputStream largerLimit = new HardLimitInputStream(inSupplier.get(), false, 1000)) {
			String result = IOTools.slurp(largerLimit, "UTF-8");
			assertThat(result).isEqualTo("hello, world");
		}

		try (HardLimitInputStream exactLimit = new HardLimitInputStream(inSupplier.get(), false, 12)) {
			String result = IOTools.slurp(exactLimit, "UTF-8");
			assertThat(result).isEqualTo("hello, world");
		}

		try (HardLimitInputStream lesserLimit = new HardLimitInputStream(inSupplier.get(), false, 11)) {
			String result = IOTools.slurp(lesserLimit, "UTF-8");
			throw new RuntimeException("This code should not have been reached.");
		} catch (IndexOutOfBoundsException expected) {
			// Ignore
		}
	}

}
