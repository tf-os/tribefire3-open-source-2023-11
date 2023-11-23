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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.braintribe.model.bvd.math.Round;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.RoundVde;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.DateOffsetUnit;

/**
 * Provides tests for {@link RoundVde}.
 * 
 */
public class RoundVdeTest extends AbstractApproximateVdeTest {

	@Test(expected = VdeRuntimeException.class)
	public void testNullOperandsRound() throws Exception {

		Round approximate = $.round();
		testNullEmptyOperands(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testNullPrecisionRound() throws Exception {

		Round approximate = $.round();
		approximate.setValue(null);
		approximate.setPrecision(4);

		testNullEmptyOperands(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testNullValueRound() throws Exception {

		Round approximate = $.round();
		approximate.setValue(4);
		approximate.setPrecision(null);

		testNullEmptyOperands(approximate);
	}

	@Test
	public void testIntegerRound() throws Exception {

		Round approximate = $.round();

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
	public void testLongRound() throws Exception {

		Round approximate = $.round();
		List<Object> precisionList = getLongPrecisionOperandsRound();

		// positive
		approximate.setValue(new Long(5));
		Long expectedValue = new Long(6);
		for (Object precision : precisionList) {
			approximate.setPrecision(precision);

			Object result = evaluate(approximate);
			validateLongResult(result, expectedValue);
		}

		// negative
		approximate.setValue(new Long(-5));
		expectedValue = new Long(-4);
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
	public void testFloatRound() throws Exception {

		Round approximate = $.round();
		List<Object> precisionList = getFloatPrecisionOperandsRound();

		// positive
		approximate.setValue(new Float(5.3));
		Float expectedValue = new Float(6);
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
	public void testDoubleRound() throws Exception {

		Round approximate = $.round();
		List<Object> precisionList = getDoublePrecisionOperandsRound();

		// positive
		approximate.setValue(new Double(5.3));
		Double expectedValue = new Double(6);
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
	public void testDecimalRound() throws Exception {

		Round approximate = $.round();
		List<Object> precisionList = getDecimalPrecisionOperandsRound();

		// positive
		approximate.setValue(new BigDecimal(5.3));
		BigDecimal expectedValue = new BigDecimal(6);
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

	@Test
	public void testDecimalRoundFraction() throws Exception {

		Round approximate = $.round();
		approximate.setValue(new BigDecimal(5.3));
		BigDecimal expectedValue = new BigDecimal(5.6, new MathContext(2, RoundingMode.HALF_UP));
		approximate.setPrecision(new Double(0.8));

		Object result = evaluate(approximate);
		validateDecimalResult(result, expectedValue);
	}

	@Test
	public void testDecimalRoundNonTerminatingFraction() throws Exception {
		Round approximate = $.round();
		approximate.setValue(new BigDecimal(1));
		BigDecimal expectedValue = new BigDecimal(0.9, new MathContext(2, RoundingMode.HALF_UP)).stripTrailingZeros();
		approximate.setPrecision(new Double(0.3));

		Object result = evaluate(approximate);
		validateDecimalResult(result, expectedValue);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateMonthRoundDateOffsetFail() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.month);
		offset.setValue(20);
		approximate.setPrecision(offset);

		evaluate(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateDayRoundDateOffsetFail() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.day);
		offset.setValue(40);
		approximate.setPrecision(offset);

		evaluate(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateHourRoundDateOffsetFail() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.hour);
		offset.setValue(40);
		approximate.setPrecision(offset);

		evaluate(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateMinuteRoundDateOffsetFail() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.minute);
		offset.setValue(70);
		approximate.setPrecision(offset);

		evaluate(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateSecondRoundDateOffsetFail() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.second);
		offset.setValue(70);
		approximate.setPrecision(offset);

		evaluate(approximate);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testDateMilliSecondRoundDateOffsetFail() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.millisecond);
		offset.setValue(7000);
		approximate.setPrecision(offset);

		evaluate(approximate);
	}

	@Test
	public void testDateYearRound() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
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
	public void testDateMonthRound() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
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
	public void testDateDayRound() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
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
	public void testDateHourRound() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
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
	public void testDateMinuteRound() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
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
	public void testDateSecondRound() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
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
	public void testDateMilliSecondRound() throws Exception {
		Date date = getSampleDate();
		Round approximate = $.round();
		approximate.setValue(date);
		DateOffset offset = DateOffset.T.create();
		offset.setOffset(DateOffsetUnit.millisecond);
		offset.setValue(2);
		approximate.setPrecision(offset);

		Object result = evaluate(approximate);
		Date expectedResult = getExpectedDateMilliSecondOffset();
		validateDate(result, expectedResult);
	}

	private Date getExpectedDateYearOffset() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2012);
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
		cal.set(Calendar.DAY_OF_MONTH, 8);
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
		cal.set(Calendar.HOUR_OF_DAY, 16);
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
		cal.set(Calendar.MINUTE, 34);
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
		cal.set(Calendar.SECOND, 12);
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
		cal.set(Calendar.MILLISECOND, 40);
		return cal.getTime();
	}

	private List<Object> getLongPrecisionOperandsRound() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getFloatPrecisionOperandsRound() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Float(2.0));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getDoublePrecisionOperandsRound() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Double(2.0));
		result.add(new Float(2.0));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getDecimalPrecisionOperandsRound() {
		List<Object> result = new ArrayList<Object>();
		result.add(new BigDecimal(2.0));
		result.add(new Double(2.0));
		result.add(new Float(2.0));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

}
