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

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

import com.braintribe.model.bvd.convert.ToDecimal;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToDecimalVde;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link ToDecimalVde}.
 * 
 */
public class ToDecimalVdeTest extends VdeTest {

	@Test
	public void testStringOperandNullFormatToDecimalConvert() throws Exception {

		ToDecimal convert = $.toDecimal();
		convert.setOperand("4");

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(BigDecimal.class);
		assertThat(result).isEqualTo(new BigDecimal(4));
	}

	@Test
	public void testStringOperandStringFormatToDecimalConvert() throws Exception {

		ToDecimal convert = $.toDecimal();
		convert.setOperand("4");
		convert.setFormat("precision=1 roundingMode=HALF_UP"); // follows format of MathContext

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(BigDecimal.class);
		assertThat(result).isEqualTo(new BigDecimal(4));
	}

	@Test(expected = VdeRuntimeException.class)
	public void testStringOperandStringFormatToDecimalConvertFormatFail() throws Exception {

		ToDecimal convert = $.toDecimal();
		convert.setOperand("4");
		convert.setFormat("."); // wrong format

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(BigDecimal.class);
		assertThat(result).isEqualTo(new BigDecimal(4));
	}

	@Test(expected = VdeRuntimeException.class)
	public void testStringOperandStringFormatToDecimalConvertFormatTypeFail() throws Exception {

		ToDecimal convert = $.toDecimal();
		convert.setOperand("4");
		convert.setFormat(new Integer(3)); // wrong format type

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(BigDecimal.class);
		assertThat(result).isEqualTo(new BigDecimal(4));
	}

	@Test
	public void testBooleanOperandNullFormatToDecimalConvert() throws Exception {

		ToDecimal convert = $.toDecimal();
		convert.setOperand(true);

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(BigDecimal.class);
		assertThat(result).isEqualTo(new BigDecimal(1));

		convert.setOperand(false);

		result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(BigDecimal.class);
		assertThat(result).isEqualTo(new BigDecimal(0));
	}

	@Test(expected = VdeRuntimeException.class)
	public void testBooleanOperandRandomFormatToDecimalConvert() throws Exception {

		ToDecimal convert = $.toDecimal();
		convert.setOperand(true);
		convert.setFormat(" ");

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(BigDecimal.class);
		assertThat(result).isEqualTo(new BigDecimal(1));
	}

	@Test(expected = VdeRuntimeException.class)
	public void testRandomOperandNullFormatToDecimalConvertTypeFail() throws Exception {

		ToDecimal convert = $.toDecimal();
		convert.setOperand(new Date()); // only string, boolean allowed
		evaluate(convert);
	}

}
