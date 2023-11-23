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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.braintribe.model.bvd.math.Add;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.AddVde;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.DateOffsetUnit;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.model.time.TimeZoneOffset;

/**
 * Provides tests for {@link AddVde}.
 * 
 */
public class AddVdeTest extends AbstractArithmeticVdeTest {

	@Test(expected = VdeRuntimeException.class)
	public void testNullOperandsAdd() throws Exception {

		Add math = $.add();

		testNullEmptyOperands(math);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testEmptyOperandsAdd() throws Exception {

		Add math = $.add();
		List<Object> operands = new ArrayList<Object>();
		math.setOperands(operands);

		testNullEmptyOperands(math);
	}

	@Test
	public void testSingleOperandAdd() throws Exception {

		Add math = $.add();
		List<Object> operands = new ArrayList<Object>();
		operands.add(new Integer(3));
		math.setOperands(operands);

		testSingleOperand(math);
	}

	@Test
	public void testIntegerAdd() throws Exception {

		Add math = $.add();
		List<Object> operands = new ArrayList<Object>();
		operands.add(new Integer(2));
		operands.add(new Integer(1));
		operands.add(new Integer(2));
		math.setOperands(operands);

		Object result = evaluate(math);
		validateIntegerResult(result);
	}

	@Test
	public void testLongAdd() throws Exception {

		Add math = $.add();
		List<List<Object>> operandsAdd = getLongOperandsAdd();

		for (List<Object> operandList : operandsAdd) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateLongResult(result);
		}

		operandsAdd = getLongOperandsAdd();
		Collections.reverse(operandsAdd);

		for (List<Object> operandList : operandsAdd) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateLongResult(result);
		}
	}

	@Test
	public void testFloatAdd() throws Exception {

		Add math = $.add();
		List<List<Object>> operandsAdd = getFloatOperandsAdd();

		for (List<Object> operandList : operandsAdd) {
			math.setOperands(operandList);
			Object result = evaluate(math);
			validateFloatResult(result);
		}

		operandsAdd = getFloatOperandsAdd();
		Collections.reverse(operandsAdd);

		for (List<Object> operandList : operandsAdd) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateFloatResult(result);
		}
	}

	@Test
	public void testDoubleAdd() throws Exception {

		Add math = $.add();
		List<List<Object>> operandsAdd = getDoubleOperandsAdd();

		for (List<Object> operandList : operandsAdd) {
			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDoubleResult(result);
		}

		operandsAdd = getDoubleOperandsAdd();
		Collections.reverse(operandsAdd);

		for (List<Object> operandList : operandsAdd) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDoubleResult(result);
		}
	}

	@Test
	public void testDecimalAdd() throws Exception {
		Add math = $.add();
		List<List<Object>> operandsAdd = getDecimalOperandsAdd();

		for (List<Object> operandList : operandsAdd) {
			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDecimalResult(result);
		}

		operandsAdd = getDecimalOperandsAdd();
		Collections.reverse(operandsAdd);

		for (List<Object> operandList : operandsAdd) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDecimalResult(result);
		}
	}

	@Test
	public void testDatePlusDateOffSet() throws Exception {

		Add math = $.add();
		Calendar cal = getDefaultYearCalendar();
		Date date = cal.getTime();

		int length = DateOffsetUnit.values().length;
		for (int i = 0; i < length; i++) {

			List<Object> operands = new ArrayList<Object>();
			operands.add(date);
			DateOffset offset = DateOffset.T.create();
			offset.setOffset(DateOffsetUnit.values()[i]);
			offset.setValue(10);
			operands.add(offset);
			math.setOperands(operands);

			Object result = evaluate(math);
			validateDate(result, offset);
		}
	}

	@Test
	public void testDatePlusTimeZoneOffSet() throws Exception {

		Add math = $.add();
		Calendar cal = getDefaultYearCalendar();
		Date date = cal.getTime();

		List<Object> operands = new ArrayList<Object>();
		operands.add(date);
		TimeZoneOffset offset = TimeZoneOffset.T.create();
		offset.setMinutes(30);
		operands.add(offset);
		math.setOperands(operands);

		Object result = evaluate(math);

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Date.class);
		Calendar calResult = Calendar.getInstance();
		calResult.setTime((Date) result);
		assertThat(calResult.get(Calendar.MINUTE)).isEqualTo(offset.getMinutes());
	}

	@Test
	public void testDatePlusTimeSpan() throws Exception {

		Add math = $.add();
		Calendar cal = getDefaultYearCalendar();
		Date date = cal.getTime();

		int length = TimeUnit.values().length;
		for (int i = 0; i < length; i++) {

			List<Object> operands = new ArrayList<Object>();
			operands.add(date);
			TimeSpan span = TimeSpan.T.create();
			span.setUnit(TimeUnit.values()[i]);
			span = getSpanForDateAdd(span);
			operands.add(span);
			math.setOperands(operands);

			Object result = evaluate(math);
			validateDate(result, span);
		}
	}

	@Test
	public void testTimeSpanPlusTimeSpan() throws Exception {

		Add math = $.add();
		int length = TimeUnit.values().length;

		for (int i = 0; i < length; i++) {

			TimeUnit firstTimeUnit = TimeUnit.values()[i];
			if (firstTimeUnit == TimeUnit.planckTime) {
				continue;
			}

			TimeSpan firstSpan = TimeSpan.T.create();
			firstSpan.setUnit(firstTimeUnit);
			firstSpan = getSpanforTimeSpanAdd(firstSpan);

			for (int j = 0; j < length; j++) {

				TimeUnit secondTimeUnit = TimeUnit.values()[j];
				if (secondTimeUnit.compareTo(firstTimeUnit) < 0) {
					continue;
				}
				TimeSpan secondSpan = TimeSpan.T.create();
				secondSpan.setUnit(secondTimeUnit);
				secondSpan = getSpanforTimeSpanAdd(secondSpan);

				List<Object> operands = new ArrayList<Object>();
				operands.add(firstSpan);
				operands.add(secondSpan);
				math.setOperands(operands);

				Object result = evaluate(math);
				validateTimeSpanAdd(firstTimeUnit, result);

			}

		}
	}

	private void validateTimeSpanAdd(TimeUnit targetTimeUnit, Object result) {
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

	private void validateDate(Object result, DateOffset offset) {

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Date.class);
		Calendar cal = Calendar.getInstance();
		cal.setTime((Date) result);

		int expectedValue = offset.getValue();
		int actual = 0;
		switch (offset.getOffset()) {
			case year:
				actual = cal.get(Calendar.YEAR);
				expectedValue += 99;
				break;
			case month:
				actual = cal.get(Calendar.MONTH) + 1;
				break;
			case day:
				actual = cal.get(Calendar.DAY_OF_MONTH);
				break;
			case hour:
				actual = cal.get(Calendar.HOUR_OF_DAY);
				break;
			case minute:
				actual = cal.get(Calendar.MINUTE);
				break;
			case second:
				actual = cal.get(Calendar.SECOND);
				break;
			case millisecond:
				actual = cal.get(Calendar.MILLISECOND);
				break;
		}
		assertThat(actual).isEqualTo(expectedValue);
	}

	private void validateDate(Object result, TimeSpan span) {

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Date.class);
		Calendar cal = Calendar.getInstance();
		cal.setTime((Date) result);

		double expectedValue = span.getValue(); // override when needed
		double actual = 0;
		switch (span.getUnit()) {
			case day:
				actual = cal.get(Calendar.DAY_OF_MONTH);
				break;
			case hour:
				actual = cal.get(Calendar.HOUR_OF_DAY);
				break;
			case minute:
				actual = cal.get(Calendar.MINUTE);
				break;
			case second:
				actual = cal.get(Calendar.SECOND);
				break;
			case milliSecond:
				actual = cal.get(Calendar.MILLISECOND);
				break;
			case microSecond:
				actual = cal.get(Calendar.MILLISECOND);
				expectedValue = 10;
				break;
			case nanoSecond:
				actual = cal.get(Calendar.MILLISECOND);
				expectedValue = 10;
				break;
			case planckTime:
				actual = 0;
				break;
		}
		assertThat(actual).isEqualTo(expectedValue);
	}

	private TimeSpan getSpanForDateAdd(TimeSpan span) {

		switch (span.getUnit()) {
			case day:
				span.setValue(10);
				break;
			case hour:
				span.setValue(10);
				break;
			case minute:
				span.setValue(10);
				break;
			case second:
				span.setValue(10);
				break;
			case milliSecond:
				span.setValue(10);
				break;
			case microSecond:
				span.setValue(10000);
				break;
			case nanoSecond:
				span.setValue(10000000);
				break;
			case planckTime:
				span.setValue(0); // just for fun
				break;
		}
		return span;
	}

	private TimeSpan getSpanforTimeSpanAdd(TimeSpan span) {
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
		return span;
	}

	private List<List<Object>> getLongOperandsAdd() {
		List<List<Object>> result = new ArrayList<List<Object>>();

		List<Object> list = new ArrayList<Object>();
		list.add(new Long(2));
		list.add(new Long(3));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Long(2));
		list.add(new Integer(3));
		result.add(list);

		return result;
	}

	private List<List<Object>> getFloatOperandsAdd() {
		List<List<Object>> result = new ArrayList<List<Object>>();

		List<Object> list = new ArrayList<Object>();
		list.add(new Float(2));
		list.add(new Float(3));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Float(2));
		list.add(new Integer(3));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Float(2));
		list.add(new Long(3));
		result.add(list);

		return result;
	}

	private List<List<Object>> getDoubleOperandsAdd() {
		List<List<Object>> result = new ArrayList<List<Object>>();

		List<Object> list = new ArrayList<Object>();
		list.add(new Double(2));
		list.add(new Double(3));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Double(2));
		list.add(new Integer(3));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Double(2));
		list.add(new Long(3));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Double(2));
		list.add(new Float(3));
		result.add(list);

		return result;
	}

	private List<List<Object>> getDecimalOperandsAdd() {
		List<List<Object>> result = new ArrayList<List<Object>>();

		List<Object> list = new ArrayList<Object>();
		list.add(new BigDecimal(2));
		list.add(new BigDecimal(3));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new BigDecimal(2));
		list.add(new Integer(3));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new BigDecimal(2));
		list.add(new Long(3));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new BigDecimal(2));
		list.add(new Float(3));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new BigDecimal(2));
		list.add(new Double(3));
		result.add(list);

		return result;
	}
}
