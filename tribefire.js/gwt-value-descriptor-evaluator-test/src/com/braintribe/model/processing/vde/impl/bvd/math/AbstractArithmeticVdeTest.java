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
package com.braintribe.model.processing.vde.impl.bvd.math;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Calendar;

import com.braintribe.model.bvd.math.ArithmeticOperation;
import com.braintribe.model.processing.vde.test.VdeTest;

public abstract class AbstractArithmeticVdeTest extends VdeTest {

	protected void validateIntegerResult(Object result, Integer expected) {
		validateNonNull(result);
		assertThat(result).isInstanceOf(Integer.class);
		assertThat(result).isEqualTo(expected);
	}

	protected void validateIntegerResult(Object result) {
		validateIntegerResult(result, new Integer(5));
	}

	protected void validateLongResult(Object result, Long expected) {
		validateNonNull(result);
		assertThat(result).isInstanceOf(Long.class);
		assertThat(result).isEqualTo(expected);
	}

	protected void validateLongResult(Object result) {
		validateLongResult(result, new Long(5));
	}

	protected void validateFloatResult(Object result, Float expected) {
		validateNonNull(result);
		assertThat(result).isInstanceOf(Float.class);
		assertThat(result).isEqualTo(expected);
	}
	protected void validateFloatResult(Object result) {
		validateFloatResult(result, new Float(5));
	}

	protected void validateDoubleResult(Object result, Double expected) {
		validateNonNull(result);
		assertThat(result).isInstanceOf(Double.class);
		assertThat(result).isEqualTo(expected);
	}

	protected void validateDoubleResult(Object result) {
		validateDoubleResult(result, new Double(5));
	}

	protected void validateDecimalResult(Object result, BigDecimal expected) {
		validateNonNull(result);
		assertThat(result).isInstanceOf(BigDecimal.class);
		assertThat(result).isEqualTo(expected);
	}

	private void validateNonNull(Object result) {
		assertThat(result).isNotNull();
	}

	protected void validateDecimalResult(Object result) {
		validateDecimalResult(result, new BigDecimal(5));
	}

	protected void testNullEmptyOperands(ArithmeticOperation operation) throws Exception {
		evaluate(operation);
	}

	protected void testSingleOperand(ArithmeticOperation operation) throws Exception {
		Object operand = operation.getOperands().get(0);
		Object result = evaluate(operation);

		validateNonNull(result);
		assertThat(result).isInstanceOf(operand.getClass());
		assertThat(result).isEqualTo(operand);
	}

	protected Calendar getDefaultYearCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 100);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	protected Calendar getDefaultTenYearCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 110);
		cal.set(Calendar.MONTH, 9);
		cal.set(Calendar.DAY_OF_MONTH, 10);
		cal.set(Calendar.HOUR_OF_DAY, 10);
		cal.set(Calendar.MINUTE, 10);
		cal.set(Calendar.SECOND, 10);
		cal.set(Calendar.MILLISECOND, 10);
		return cal;
	}
}
