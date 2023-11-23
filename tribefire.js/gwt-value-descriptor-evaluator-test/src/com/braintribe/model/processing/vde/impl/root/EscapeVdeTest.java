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
package com.braintribe.model.processing.vde.impl.root;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.value.Escape;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link Escape}.
 * 
 */
public class EscapeVdeTest extends VdeTest {

	/**
	 * Validate that a {@link Escape} that contains {@link ValueDescriptor}, will evaluate to the same {@link ValueDescriptor}.
	 */
	@Test
	public void testNonNullEvaluation() throws Exception {

		// init test data
		final Escape vd = $.escape();
		final Now randomVd = $.now();
		vd.setValue(randomVd);

		// validate input

		// validate that data is not null
		assertThat(randomVd).isNotNull();
		assertThat(vd).isNotNull();

		// run the evaluate method
		Object result = evaluate(vd);

		// validate output
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(ValueDescriptor.class);

		assertThat(result.getClass()).isSameAs(randomVd.getClass());
		// assertThat(result).isEqualTo(randomVd);
		assertThat(result).isNotEqualTo(vd);
	}

	/**
	 * Validate that a {@link Escape} that contains null, will evaluate to null.
	 */
	@Test
	public void testNullEvaluation() throws Exception {

		// init test data
		final Escape vd = $.escape();

		// validate input
		// validate that data is not null
		assertThat(vd).isNotNull();

		// run the evaluate method
		Object result = evaluate(vd);

		// validate output
		assertThat(result).isNull();
	}

}
