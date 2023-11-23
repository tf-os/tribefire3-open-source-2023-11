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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.braintribe.model.bvd.math.Add;
import com.braintribe.model.bvd.math.Floor;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.FloorVde;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.DateOffsetUnit;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * Provides tests for {@link FloorVde}.
 * 
 */
public class FloorVdeTest extends AbstractApproximateVdeTest {

	@Test(expected = VdeRuntimeException.class)
	public void testNullOperandsFloor() throws Exception {

		Floor approximate = $.floor();
		testNullEmptyOperands(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testNullPrecisionFloor() throws Exception {

		Floor approximate = $.floor();
		approximate.setValue(null);
		approximate.setPrecision(4);

		testNullEmptyOperands(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testNullValueFloor() throws Exception {

		Floor approximate = $.floor();
		approximate.setValue(4);
		approximate.setPrecision(null);

		testNullEmptyOperands(approximate);
	}

	@Test
	public void testIntegerFloor() throws Exception {

		Floor approximate = $.floor();

		approximate.setValue(new Integer(3));
		approximate.setPrecision(new Integer(1));

		Object result = evaluate(approximate);
		validateIntegerResult(result, new Integer(3));

		approximate.setValue(new Integer(-3));
		approximate.setPrecision(new Integer(1));

		result = evaluate(approximate);
		validateIntegerResult(result, new Integer(-3));

		approximate.setValue(new Integer(0));
		approximate.setPrecision(new Integer(1));

		result = evaluate(approximate);
		validateIntegerResult(result, new Integer(0));
	}

	@Test
	public void testLongFloor() throws Exception {

		Floor approximate = $.floor();
		List<Object> precisionList = getLongPrecisionOperandsFloor();

		// positive
		approximate.setValue(new Long(5));
		Long expectedValue = new Long(4);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateLongResult(result, expectedValue);
		}

		// negative
		approximate.setValue(new Long(-5));
		expectedValue = new Long(-6);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateLongResult(result, expectedValue);
		}

		// zero
		approximate.setValue(new Long(0));
		expectedValue = new Long(0);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateLongResult(result, expectedValue);
		}

	}

	@Test
	public void testFloatFloor() throws Exception {

		Floor approximate = $.floor();
		List<Object> precisionList = getFloatPrecisionOperandsFloor();

		// positive
		approximate.setValue(new Float(5.3));
		Float expectedValue = new Float(4);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateFloatResult(result, expectedValue);
		}

		// negative
		approximate.setValue(new Float(-5.3));
		expectedValue = new Float(-6);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateFloatResult(result, expectedValue);
		}

		// zero
		approximate.setValue(new Float(0));
		expectedValue = new Float(0);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateFloatResult(result, expectedValue);
		}

	}

	@Test
	public void testDoubleFloor() throws Exception {

		Floor approximate = $.floor();
		List<Object> precisionList = getDoublePrecisionOperandsFloor();

		// positive
		approximate.setValue(new Double(5.3));
		Double expectedValue = new Double(4);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateDoubleResult(result, expectedValue);
		}

		// negative
		approximate.setValue(new Double(-5.3));
		expectedValue = new Double(-6);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateDoubleResult(result, expectedValue);
		}

		// zero
		approximate.setValue(new Double(0));
		expectedValue = new Double(0);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateDoubleResult(result, expectedValue);
		}

	}

	@Test
	public void testDecimalFloor() throws Exception {

		Floor approximate = $.floor();
		List<Object> precisionList = getDecimalPrecisionOperandsFloor();

		// positive
		approximate.setValue(new BigDecimal(5.3));
		BigDecimal expectedValue = new BigDecimal(4);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateDecimalResult(result, expectedValue);
		}

		// negative
		approximate.setValue(new BigDecimal(-5.3));
		expectedValue = new BigDecimal(-6);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateDecimalResult(result, expectedValue);
		}

		// zero
		approximate.setValue(new BigDecimal(0));
		expectedValue = new BigDecimal(0);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateDecimalResult(result, expectedValue);
		}

	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateMonthFloorDateOffsetFail() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.month);
		offset.setValue(20);
		approximate.setPrecision(offset);

		evaluate(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateDayFloorDateOffsetFail() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.day);
		offset.setValue(40);
		approximate.setPrecision(offset);

		evaluate(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateHourFloorDateOffsetFail() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.hour);
		offset.setValue(40);
		approximate.setPrecision(offset);

		evaluate(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateMinuteFloorDateOffsetFail() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.minute);
		offset.setValue(70);
		approximate.setPrecision(offset);

		evaluate(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateSecondFloorDateOffsetFail() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.second);
		offset.setValue(70);
		approximate.setPrecision(offset);

		evaluate(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateMilliSecondFloorDateOffsetFail() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.millisecond);
		offset.setValue(7000);
		approximate.setPrecision(offset);

		evaluate(approximate);
	}

	@Test
	public void testDateYearFloor() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.year);
		offset.setValue(2);
		approximate.setPrecision(offset);

		Object result = evaluate(approximate);
		Date expectedResult = getExpectedDateYearOffset();
		validateDate(result, expectedResult);
	}

	@Test
	public void testDateMonthFloor() throws Exception {
		Date date = getSampleDate();
		System.out.println(date);
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.month);
		offset.setValue(2);
		approximate.setPrecision(offset);
		Object result = evaluate(approximate);
		Date expectedResult = getExpectedDateMonthOffset();
		validateDate(result, expectedResult);
	}

	@Test
	public void testDateDayFloor() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.day);
		offset.setValue(2);
		approximate.setPrecision(offset);

		Object result = evaluate(approximate);
		Date expectedResult = getExpectedDateDayOffset();
		validateDate(result, expectedResult);
	}

	@Test
	public void testDateHourFloor() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.hour);
		offset.setValue(2);
		approximate.setPrecision(offset);

		Object result = evaluate(approximate);
		Date expectedResult = getExpectedDateHourOffset();
		validateDate(result, expectedResult);
	}

	@Test
	public void testBeginAndEndOfDay() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.hour);
		offset.setValue(24);
		approximate.setPrecision(offset);

		Object result = evaluate(approximate);
		Date expectedResult = getExpectedBeginDate();
		validateDate(result, expectedResult);

		DateOffset dayplusoneOffset = DateOffset.T.create();
		dayplusoneOffset.setOffset(DateOffsetUnit.day);
		dayplusoneOffset.setValue(1);

		Add dayplusone = Add.T.create();
		dayplusone.setOperands(CollectionTools2.asList(approximate, dayplusoneOffset));

		Object resultPlusOne = evaluate(dayplusone);
		Date expectedResultPlusOne = getExpectedEndDate();
		validateDate(resultPlusOne, expectedResultPlusOne);
	}

	@Test
	public void testDateMinuteFloor() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.minute);
		offset.setValue(2);
		approximate.setPrecision(offset);

		Object result = evaluate(approximate);
		Date expectedResult = getExpectedDateMinuteOffset();
		validateDate(result, expectedResult);
	}

	@Test
	public void testDateSecondFloor() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.second);
		offset.setValue(2);
		approximate.setPrecision(offset);

		Object result = evaluate(approximate);
		Date expectedResult = getExpectedDateSecondOffset();
		validateDate(result, expectedResult);
	}

	@Test
	public void testDateMilliSecondFloor() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.millisecond);
		offset.setValue(2);
		approximate.setPrecision(offset);

		Object result = evaluate(approximate);
		Date expectedResult = getExpectedDateMilliSecondOffset();
		validateDate(result, expectedResult);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testRandomOperandFloorFail() throws Exception {
		Date date = getSampleDate();
		Floor approximate = $.floor();
		approximate.setValue(date);
		approximate.setPrecision(date);

		evaluate(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testNegativePrecisionFloorFail() throws Exception {
		Floor approximate = $.floor();
		approximate.setValue(new Double(2.3));
		approximate.setPrecision(new Double(-2.3));

		evaluate(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testNegativePrecisionDecimalFloorFail() throws Exception {
		Floor approximate = $.floor();
		approximate.setValue(new BigDecimal(2.3));
		approximate.setPrecision(new Double(-2.3));

		evaluate(approximate);
	}
	private Date getExpectedDateYearOffset() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2010);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private Date getExpectedDateMonthOffset() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private Date getExpectedDateDayOffset() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 6);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private Date getExpectedDateHourOffset() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 7);
		cal.set(Calendar.HOUR_OF_DAY, 14);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private Date getExpectedDateMinuteOffset() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 7);
		cal.set(Calendar.HOUR_OF_DAY, 15);
		cal.set(Calendar.MINUTE, 32);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private Date getExpectedDateSecondOffset() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 7);
		cal.set(Calendar.HOUR_OF_DAY, 15);
		cal.set(Calendar.MINUTE, 33);
		cal.set(Calendar.SECOND, 10);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private Date getExpectedDateMilliSecondOffset() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 7);
		cal.set(Calendar.HOUR_OF_DAY, 15);
		cal.set(Calendar.MINUTE, 33);
		cal.set(Calendar.SECOND, 11);
		cal.set(Calendar.MILLISECOND, 38);
		return cal.getTime();
	}

	private List<Object> getLongPrecisionOperandsFloor() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getFloatPrecisionOperandsFloor() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Float(2.0));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getDoublePrecisionOperandsFloor() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Double(2.0));
		result.add(new Float(2.0));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getDecimalPrecisionOperandsFloor() {
		List<Object> result = new ArrayList<Object>();
		result.add(new BigDecimal(2.0));
		result.add(new Double(2.0));
		result.add(new Float(2.0));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private Date getExpectedBeginDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 7);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private Date getExpectedEndDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 8);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

}
