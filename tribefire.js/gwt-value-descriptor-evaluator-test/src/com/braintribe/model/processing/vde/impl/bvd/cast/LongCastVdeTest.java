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
package com.braintribe.model.processing.vde.impl.bvd.cast;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.bvd.cast.LongCast;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.cast.LongCastVde;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link LongCastVde}.
 * 
 */
public class LongCastVdeTest extends VdeTest {

	@Test
	public void testNumberToLongast() throws Exception {

		Object[] numbers = CastUtil.getAllPossibleNumberTypesArray();
		LongCast cast = $.longCast();
		for (Object number : numbers) {

			cast.setOperand(number);
			Object result = evaluate(cast);

			assertThat(result).isNotNull();
			assertThat(result).isInstanceOf(Long.class);
		}
	}

	@Test(expected = VdeRuntimeException.class)
	public void testBooleanToLongCastFail() throws Exception {
		Boolean x = new Boolean(true);

		LongCast cast = $.longCast();
		cast.setOperand(x);

		evaluate(cast);
	}
}
