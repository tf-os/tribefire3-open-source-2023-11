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

import java.util.Date;

import org.junit.Test;

import com.braintribe.model.bvd.convert.ToInteger;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToIntegerVde;
import com.braintribe.model.processing.vde.impl.misc.SalaryRange;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link ToIntegerVde}.
 * 
 */
public class ToIntegerVdeTest extends VdeTest {

	@Test
	public void testStringOperandNullFormatToIntegerConvert() throws Exception {

		ToInteger convert = $.toInteger();
		convert.setOperand("4");

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Integer.class);
		assertThat(result).isEqualTo(new Integer(4));
	}

	@Test(expected = VdeRuntimeException.class)
	public void testStringOperandRandomFormatToIntegerConvertFormatFail() throws Exception {

		ToInteger convert = $.toInteger();
		convert.setOperand("4");
		convert.setFormat(" "); // formats not taken into consideration till now

		evaluate(convert);
	}

	@Test
	public void testBooleanOperandNullFormatToIntegerConvert() throws Exception {

		ToInteger convert = $.toInteger();
		convert.setOperand(true);

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Integer.class);
		assertThat(result).isEqualTo(new Integer(1));

		convert.setOperand(false);

		result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Integer.class);
		assertThat(result).isEqualTo(new Integer(0));
	}

	@Test
	public void testEnumOperandNullFormatToIntegerConvert() throws Exception {

		ToInteger convert = $.toInteger();
		convert.setOperand(SalaryRange.low);

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Integer.class);
		assertThat(result).isEqualTo(new Integer(0));

	}

	@Test(expected = VdeRuntimeException.class)
	public void testRandomOperandNullFormatToIntegerConvertTypeFail() throws Exception {

		ToInteger convert = $.toInteger();
		convert.setOperand(new Date()); // only string, boolean allowed
		evaluate(convert);
	}

}
