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
package com.braintribe.model.processing.vde.impl.bvd.convert;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.bvd.convert.ToBoolean;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToBooleanVde;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link ToBooleanVde}.
 * 
 */
public class ToBooleanVdeTest extends VdeTest {

	@Test
	public void testStringOperandNullFomratToBooleanTrueConvert() throws Exception {

		ToBoolean convert = $.toBoolean();

		convert.setOperand("true"); // TRUE works too
		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void testStringOperandNullFomratToBooleanFalseConvert() throws Exception {

		ToBoolean convert = $.toBoolean();

		convert.setOperand("false");
		Object result = evaluate(convert);

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(false);

		convert.setOperand("xyz");
		result = evaluate(convert);

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(false);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testStringOperandRandomFomratToBooleanConvertFormatFail() throws Exception {

		ToBoolean convert = $.toBoolean();

		convert.setOperand("true");
		convert.setFormat("format"); // format is not implemented for ToBoolean
		evaluate(convert);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testRandomOperandNullFomratToBooleanConvertOperandFail() throws Exception {
		ToBoolean convert = $.toBoolean();

		convert.setOperand(new Integer(30)); // only string allowed
		evaluate(convert);
	}
}
