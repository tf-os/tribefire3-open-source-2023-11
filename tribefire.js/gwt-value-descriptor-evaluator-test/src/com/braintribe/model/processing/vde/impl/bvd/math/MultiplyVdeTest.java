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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.braintribe.model.bvd.math.Multiply;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.MultiplyVde;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

/**
 * Provides tests for {@link MultiplyVde}.
 * 
 */
public class MultiplyVdeTest extends AbstractArithmeticVdeTest {

	@Test(expected = VdeRuntimeException.class)
	public void testNullOperandsMultiply() throws Exception {

		Multiply math = $.multiply();

		testNullEmptyOperands(math);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testEmptyOperandsMultiply() throws Exception {

		Multiply math = $.multiply();
		List<Object> operands = new ArrayList<Object>();
		math.setOperands(operands);

		testNullEmptyOperands(math);
	}

	@Test
	public void testSingleOperandMultiply() throws Exception {

		Multiply math = $.multiply();
		List<Object> operands = new ArrayList<Object>();
		operands.add(new Integer(3));
		math.setOperands(operands);

		testSingleOperand(math);
	}

	@Test
	public void testIntegerMultiply() throws Exception {

		Multiply math = $.multiply();
		List<Object> operands = new ArrayList<Object>();
		operands.add(new Integer(1));
		operands.add(new Integer(5));
		math.setOperands(operands);

		Object result = evaluate(math);
		validateIntegerResult(result);
	}

	@Test
	public void testLongMultiply() throws Exception {

		Multiply math = $.multiply();
		List<List<Object>> operandsMultiply = getLongOperandsMultiply();

		for (List<Object> operandList : operandsMultiply) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateLongResult(result);
		}

		operandsMultiply = getLongOperandsMultiply();
		Collections.reverse(operandsMultiply);

		for (List<Object> operandList : operandsMultiply) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateLongResult(result);
		}
	}

	@Test
	public void testFloatMultiply() throws Exception {

		Multiply math = $.multiply();
		List<List<Object>> operandsMultiply = getFloatOperandsMultiply();

		for (List<Object> operandList : operandsMultiply) {
			math.setOperands(operandList);
			Object result = evaluate(math);
			validateFloatResult(result);
		}

		operandsMultiply = getFloatOperandsMultiply();
		Collections.reverse(operandsMultiply);

		for (List<Object> operandList : operandsMultiply) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateFloatResult(result);
		}
	}

	@Test
	public void testDoubleMultiply() throws Exception {

		Multiply math = $.multiply();
		List<List<Object>> operandsMultiply = getDoubleOperandsMultiply();

		for (List<Object> operandList : operandsMultiply) {
			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDoubleResult(result);
		}

		operandsMultiply = getDoubleOperandsMultiply();
		Collections.reverse(operandsMultiply);

		for (List<Object> operandList : operandsMultiply) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDoubleResult(result);
		}
	}

	@Test
	public void testDecimalMultiply() throws Exception {
		Multiply math = $.multiply();
		List<List<Object>> operandsMultiply = getDecimalOperandsMultiply();

		for (List<Object> operandList : operandsMultiply) {
			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDecimalResult(result);
		}

		operandsMultiply = getDecimalOperandsMultiply();
		Collections.reverse(operandsMultiply);

		for (List<Object> operandList : operandsMultiply) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDecimalResult(result);
		}
	}

	@Test
	public void testNumberMultiplyTimeSpan() throws Exception {

		Multiply math = $.multiply();
		Object[] multiples = getTimeSpanMultipliers();

		int length = TimeUnit.values().length;

		for (int i = multiples.length - 1; i > 0; i--) {

			Object number = multiples[i];
			for (int j = 0; j < length; j++) {

				TimeUnit timeUnit = TimeUnit.values()[j];
				if (timeUnit == TimeUnit.planckTime) {
					continue;
				}
				TimeSpan span = TimeSpan.T.create();
				span.setUnit(timeUnit);
				span = getSpanforTimeSpanMultiply(span, 1.0);

				List<Object> operands = new ArrayList<Object>();
				operands.add(number);
				operands.add(span);
				math.setOperands(operands);

				Object result = evaluate(math);
				validateTimeSpanMultiply(timeUnit, result);
				// check commutative property
				span = TimeSpan.T.create();
				span.setUnit(timeUnit);
				span = getSpanforTimeSpanMultiply(span, 1.0);

				operands = new ArrayList<Object>();
				operands.add(span);
				operands.add(number);
				math.setOperands(operands);

				result = evaluate(math);
				validateTimeSpanMultiply(timeUnit, result);

			}
		}
	}

	private Object[] getTimeSpanMultipliers() {
		Object[] result = new Object[5];
		result[0] = new Integer(2);
		result[1] = new Long(2);
		result[2] = new Float(2);
		result[3] = new Double(2);
		result[4] = new BigDecimal(2);
		return result;
	}

	private void validateTimeSpanMultiply(TimeUnit targetTimeUnit, Object result) {
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(TimeSpan.class);
		TimeSpan span = (TimeSpan) result;

		assertThat(span.getUnit()).isEqualTo(targetTimeUnit);
		double value = span.getValue();
		switch (span.getUnit()) {
			case day:
				assertThat(value).isEqualTo(2);
				break;
			case hour:
				assertThat(value).isEqualTo(48);
				break;
			case minute:
				assertThat(value).isEqualTo(2880);
				break;
			case second:
				assertThat(value).isEqualTo(172800);
				break;
			case milliSecond:
				assertThat(value).isEqualTo(1.728E8);
				break;
			case microSecond:
				assertThat(value).isEqualTo(1.728E11);
				break;
			case nanoSecond:
				assertThat(value).isEqualTo(1.728E14);
				break;
			case planckTime:
				// just for fun
				break;
		}

	}

	private TimeSpan getSpanforTimeSpanMultiply(TimeSpan span, double factor) {
		switch (span.getUnit()) {
			case day:
				span.setValue(1.0);
				break;
			case hour:
				span.setValue(24.0);
				break;
			case minute:
				span.setValue(24.0 * 60.0);
				break;
			case second:
				span.setValue(24.0 * 60.0 * 60.0);
				break;
			case milliSecond:
				span.setValue(24.0 * 60.0 * 60.0 * 1000.0);
				break;
			case microSecond:
				span.setValue(24.0 * 60.0 * 60.0 * 1000000.0);
				break;
			case nanoSecond:
				span.setValue(24.0 * 60.0 * 60.0 * 1000000000.0);
				break;
			case planckTime:
				span.setValue(0); // just for fun
				break;
		}
		span.setValue(span.getValue() * factor);
		return span;
	}

	private List<List<Object>> getLongOperandsMultiply() {
		List<List<Object>> result = new ArrayList<List<Object>>();

		List<Object> list = new ArrayList<Object>();
		list.add(new Long(1));
		list.add(new Long(5));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Long(5));
		list.add(new Integer(1));
		result.add(list);

		return result;
	}

	private List<List<Object>> getFloatOperandsMultiply() {
		List<List<Object>> result = new ArrayList<List<Object>>();

		List<Object> list = new ArrayList<Object>();
		list.add(new Float(5));
		list.add(new Float(1));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Float(1));
		list.add(new Integer(5));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Float(5));
		list.add(new Long(1));
		result.add(list);

		return result;
	}

	private List<List<Object>> getDoubleOperandsMultiply() {
		List<List<Object>> result = new ArrayList<List<Object>>();

		List<Object> list = new ArrayList<Object>();
		list.add(new Double(5));
		list.add(new Double(1));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Double(1));
		list.add(new Integer(5));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Double(5));
		list.add(new Long(1));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Double(5));
		list.add(new Float(1));
		result.add(list);

		return result;
	}

	private List<List<Object>> getDecimalOperandsMultiply() {
		List<List<Object>> result = new ArrayList<List<Object>>();

		List<Object> list = new ArrayList<Object>();
		list.add(new BigDecimal(5));
		list.add(new BigDecimal(1));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new BigDecimal(1));
		list.add(new Integer(5));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new BigDecimal(5));
		list.add(new Long(1));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new BigDecimal(1));
		list.add(new Float(5));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new BigDecimal(1));
		list.add(new Double(5));
		result.add(list);

		return result;
	}
}
