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

import com.braintribe.model.bvd.convert.ToLong;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToLongVde;
import com.braintribe.model.processing.vde.impl.misc.Name;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link ToLongVde}.
 * 
 */
public class ToLongVdeTest extends VdeTest {

	@Test
	public void testStringOperandNullFormatToLongConvert() throws Exception {

		ToLong convert = $.toLong();
		convert.setOperand("4");

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Long.class);
		assertThat(result).isEqualTo(new Long(4));
	}

	@Test(expected = VdeRuntimeException.class)
	public void testStringOperandRandomFormatToLongConvertFormatFail() throws Exception {

		ToLong convert = $.toLong();
		convert.setOperand("4");
		convert.setFormat(" "); // formats not taken into consideration till now

		evaluate(convert);
	}

	@Test
	public void testBooleanOperandNullFormatToLongConvert() throws Exception {

		ToLong convert = $.toLong();
		convert.setOperand(true);

		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Long.class);
		assertThat(result).isEqualTo(new Long(1));

		convert.setOperand(false);

		result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Long.class);
		assertThat(result).isEqualTo(new Long(0));
	}

	@Test
	public void testDateOperandNullFormatToLongConvertTypeFail() throws Exception {

		ToLong convert = $.toLong();
		convert.setOperand(new Date());
		Object result = evaluate(convert);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Long.class);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testRandomOperandNullFormatToLongConvertTypeFail() throws Exception {

		ToLong convert = $.toLong();
		convert.setOperand(Name.T.create()); // only string, boolean, date allowed
		evaluate(convert);
	}

}
