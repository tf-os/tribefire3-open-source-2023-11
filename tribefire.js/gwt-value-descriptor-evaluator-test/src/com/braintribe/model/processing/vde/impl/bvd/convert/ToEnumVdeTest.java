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

import com.braintribe.model.bvd.convert.ToEnum;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToEnumVde;
import com.braintribe.model.processing.vde.impl.misc.SalaryRange;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link ToEnumVde}.
 * 
 */
public class ToEnumVdeTest extends VdeTest {

	@Test
	public void testValidTypeStringOperandToEnumConvert() throws Exception {

		ToEnum convert = $.toEnum();
		convert.setTypeSignature(SalaryRange.class.getName());
		convert.setOperand("low");

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Enum.class);
		assertThat(result).isEqualTo(SalaryRange.low);
	}

	@Test
	public void testValidTypeIntegerOperandToEnumConvert() throws Exception {

		ToEnum convert = $.toEnum();
		convert.setTypeSignature(SalaryRange.class.getName());
		convert.setOperand(0);

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Enum.class);
		assertThat(result).isEqualTo(SalaryRange.low);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testValidTypeRandomOperandToEnumConvertOperandFail() throws Exception {

		ToEnum convert = $.toEnum();
		convert.setTypeSignature(SalaryRange.class.getName());
		convert.setOperand(new Date()); // only string or int are allowed

		evaluate(convert);
	}

	@Test(expected = GenericModelException.class)
	public void testRandomTypeNullOperandToEnumConvertTypeSignatureFail() throws Exception {

		ToEnum convert = $.toEnum();
		convert.setTypeSignature("random");

		evaluate(convert);
	}
}
