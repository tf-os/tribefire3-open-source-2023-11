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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import com.braintribe.model.bvd.convert.ToString;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToStringVde;
import com.braintribe.model.processing.vde.impl.VDGenerator;
import com.braintribe.model.processing.vde.impl.misc.Person;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link ToStringVde}.
 * 
 */
public class ToStringVdeTest extends VdeTest {

	public static VDGenerator $ = new VDGenerator();

	@Test
	public void testDateOperandStringFormatToDateConvert() throws Exception {

		ToString convert = $.ToString();
		Date date = Calendar.getInstance().getTime();
		convert.setOperand(date);
		convert.setFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"); // valid format

		Object result = evaluate(convert);
		validateStringResult(result);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateOperandStringFormatToDateConvertFormatFail() throws Exception {

		ToString convert = $.ToString();
		Date date = Calendar.getInstance().getTime();
		convert.setOperand(date);
		convert.setFormat("wrong"); // wrong format

		evaluate(convert);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateOperandStringFormatToDateConvertFormatTypeFail() throws Exception {

		ToString convert = $.ToString();
		Date date = Calendar.getInstance().getTime();
		convert.setOperand(date);
		convert.setFormat(new Integer(4)); // wrong format type

		evaluate(convert);
	}

	@Test
	public void testDateOperandNullFormatToDateConvert() throws Exception {

		ToString convert = $.ToString();
		Date date = Calendar.getInstance().getTime();
		convert.setOperand(date);

		Object result = evaluate(convert);
		validateStringResult(result);
	}

	@Test
	public void testRandomOperandNullFormatToDateConvert() throws Exception {

		ToString convert = $.ToString();
		convert.setOperand(Person.T.create());

		Object result = evaluate(convert);
		validateStringResult(result);

	}

	@Test
	public void testNumberOperandNullFormatToDateConvert() throws Exception {

		ToString convert = $.ToString();
		convert.setOperand(new Integer(3));

		Object result = evaluate(convert);
		validateStringResult(result);

		convert.setOperand(new BigDecimal(2));

		result = evaluate(convert);
		validateStringResult(result);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testNumberOperandRandomFormatToDateConvert() throws Exception {
		ToString convert = $.ToString();
		convert.setOperand(new Integer(3));
		convert.setFormat(new Date());

		evaluate(convert);
	}

	@Test
	public void testNumberOperandNumberFormatToDateConvert() throws Exception {
		Locale.setDefault(Locale.ENGLISH);

		ToString convert = $.ToString();
		String format = "###.##";
		convert.setOperand(new Integer(3));
		convert.setFormat(format);

		Object result = evaluate(convert);
		validateStringResult(result, "3");

		convert.setOperand(new BigDecimal(2));

		result = evaluate(convert);
		validateStringResult(result, "2");

		convert.setOperand(new Double(2.423));

		result = evaluate(convert);
		validateStringResult(result, "2.42");
	}

	private void validateStringResult(Object result) {
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(String.class);
	}

	private void validateStringResult(Object result, String expectedResult) {
		validateStringResult(result);
		assertThat((String) result).isEqualTo(expectedResult);
	}
}
