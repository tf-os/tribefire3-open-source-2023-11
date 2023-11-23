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
package com.braintribe.model.processing.vde.evaluator.impl.approximate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.approximate.ApproximateEvalExpert;
import com.braintribe.model.processing.vde.evaluator.impl.approximate.round.DecimalRound;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.DateOffsetUnit;
import com.braintribe.utils.format.api.CustomDateFormat;
import com.braintribe.utils.format.lcd.FormatTool;

/**
 * Util class that assists with evaluation, used by some implementations of
 * {@link ApproximateEvalExpert}
 * 
 */
public class ApproximateVdeUtil {


	/**
	 * Validate that date offset is within correct range
	 * 
	 */
	public static boolean validateDateOffset(DateOffset offset){
		
		int value = offset.getValue();
		if(value <= 0){
			return false;
		}
		
		switch(offset.getOffset()){
			case year:
				return true;
			case month:
				if(value <= 12){
					return true;
				}
				break;	
			case day:
				if(value <= 31){
					return true;
				}
				break;
			case hour:
				if(value <= 24){
					return true;
				}
				break;
			case minute:
				if(value <= 60){
					return true;
				}
				break;
			case second:
				if(value <= 60){
					return true;
				}
				break;	
			case millisecond:
				if(value <= 1000){
					return true;
				}
				break;
			default:
				break;
		}
		
		return false;
	}
	
	

	/**
	 * A recursive function that computes full fractional value of a date with
	 * relative to an offset
	 * 
	 */
	public static double getSmallerUnitValue(Date date, DateOffsetUnit offsetUnit) throws VdeRuntimeException {

		double fractionRealValue = 0.0;
		int fractionMaxValue = 0;
		double smallerFractions = 0.0;
		CustomDateFormat dateFormat = FormatTool.getExpert().getDateFormat();
		switch (offsetUnit) {
			case year:
				fractionMaxValue = 12;
				fractionRealValue = dateFormat.getMonth(date) + 1;
				smallerFractions = getSmallerUnitValue(date, DateOffsetUnit.month);
				break;
			case month:
				fractionMaxValue = dateFormat.getDayMax(date);
				fractionRealValue = dateFormat.getDay(date);
				smallerFractions = getSmallerUnitValue(date, DateOffsetUnit.day);
				break;
			case day:
				fractionMaxValue = 24;
				fractionRealValue = dateFormat.getDay(date);
				smallerFractions = getSmallerUnitValue(date, DateOffsetUnit.hour);
				break;
			case hour:
				fractionMaxValue = 60;
				fractionRealValue = dateFormat.getMinute(date);
				smallerFractions = getSmallerUnitValue(date, DateOffsetUnit.minute);
				break;
			case minute:
				fractionMaxValue = 60;
				fractionRealValue = dateFormat.getSecond(date);
				smallerFractions = getSmallerUnitValue(date, DateOffsetUnit.second);
				break;
			case second:
				fractionMaxValue = 1000;
				fractionRealValue = dateFormat.getMilliSecond(date);
				smallerFractions = getSmallerUnitValue(date, DateOffsetUnit.millisecond);
				break;
			case millisecond:
				return 0.0;
			default:
				throw new VdeRuntimeException("Unknown date offset" + offsetUnit);
		}

		double fractionOfTarget = fractionRealValue / fractionMaxValue + smallerFractions / fractionMaxValue;
		return fractionOfTarget;
	}

	/**
	 * sets a component of the date object indicated by the offsetUnit to a new
	 * value
	 * 
	 */
	public static Date setOriginalDateComponentValue(Date date, DateOffsetUnit offsetUnit, int value) throws VdeRuntimeException {
		CustomDateFormat dateFormat = FormatTool.getExpert().getDateFormat();
		switch (offsetUnit) {
			case year:
				return dateFormat.setYear(date, value);
			case month:
				return dateFormat.setMonth(date, value); // TODO verify this in
															// GWT
			case day:
				return dateFormat.setDay(date, value);
			case hour:
				return dateFormat.setHour(date, value);
			case minute:
				return dateFormat.setMinute(date, value);
			case second:
				return dateFormat.setSecond(date, value);
			case millisecond:
				return dateFormat.setMilliSecond(date, value);
			default:
				throw new VdeRuntimeException("Unknown date offset" + offsetUnit);
		}
	}

	/**
	 * resets all date components below than offsetUnit to a lowest possible
	 * value
	 * 
	 */
	public static Date resetBelowThreshold(Date date, DateOffsetUnit offsetUnit) throws VdeRuntimeException {
		CustomDateFormat dateFormat = FormatTool.getExpert().getDateFormat();
		switch (offsetUnit) {
			case year:
				date = dateFormat.setMonth(date, 1); // TODO verify this in GWT
				//$FALL-THROUGH$
			case month:
				date = dateFormat.setDay(date, 1);
				//$FALL-THROUGH$
			case day:
				date = dateFormat.setHour(date, 0);
				//$FALL-THROUGH$
			case hour:
				date = dateFormat.setMinute(date, 0);
				//$FALL-THROUGH$
			case minute:
				date = dateFormat.setSecond(date, 0);
				//$FALL-THROUGH$
			case second:
				date = dateFormat.setMilliSecond(date, 0);
				//$FALL-THROUGH$
			case millisecond:
				return date;
			default:
				throw new VdeRuntimeException("Unknown date offset" + offsetUnit);
		}
	}

	/**
	 * get a date component that corresponds to a DateOffsetUnit
	 * 
	 */
	public static int getOriginalDateComponentValue(Date date, DateOffsetUnit offsetUnit) throws VdeRuntimeException {

		CustomDateFormat dateFormat = FormatTool.getExpert().getDateFormat();
		int origValue = 0;
		switch (offsetUnit) {
			case year:
				origValue = dateFormat.getYear(date);
				break;
			case month:
				origValue = dateFormat.getMonth(date);
				break;
			case day:
				origValue = dateFormat.getDay(date);
				break;
			case hour:
				origValue = dateFormat.getHour(date);
				break;
			case minute:
				origValue = dateFormat.getMinute(date);
				break;
			case second:
				origValue = dateFormat.getSecond(date);
				break;
			case millisecond:
				origValue = dateFormat.getMilliSecond(date);
				break;
			default:
				throw new VdeRuntimeException("Unknown date offset" + offsetUnit);
		}
		return origValue;
	}

	/**
	 * compute floor value of a decimal with respect to any type for precision
	 * 
	 */
	public static BigDecimal getDecimalFloor(BigDecimal value, Number numberPrecision) throws VdeRuntimeException {

		final BigDecimal zero = new BigDecimal(0.0);
		BigDecimal[] array = getDecimalDivisionParts(value, numberPrecision);
		BigDecimal precision = array[0];
		BigDecimal integerPart = array[1];
		BigDecimal fractionPart = array[2];

		BigDecimal result = value;
		if (fractionPart.compareTo(zero) != 0.0) {
			result = integerPart.multiply(precision);
		}

		return result;
	}

	/**
	 * compute ceil value of a decimal with respect to any type for precision
	 * 
	 */
	public static BigDecimal getDecimalCeil(BigDecimal value, Number numberPrecision) throws VdeRuntimeException {

		final BigDecimal zero = new BigDecimal(0.0);
		BigDecimal[] array = getDecimalDivisionParts(value, numberPrecision);
		BigDecimal precision = array[0];
		BigDecimal integerPart = array[1];
		BigDecimal fractionPart = array[2];

		BigDecimal result = value;
		if (fractionPart.compareTo(zero) != 0.0) {
			result = integerPart.add(new BigDecimal(1.0)).multiply(precision);
		}

		return result;
	}

	/**
	 * compute round value of a decimal with respect to any type for precision
	 * 
	 */
	public static BigDecimal getDecimalRound(BigDecimal value, Number numberPrecision) throws VdeRuntimeException {
		return getDecimalRound(value, numberPrecision, true);
	}

	/**
	 * compute round value of a decimal with respect to any type for precision,
	 * the boolean value is used to adjust for negative numbers rounding in
	 * {@link DecimalRound}
	 * 
	 */
	public static BigDecimal getDecimalRound(BigDecimal value, Number numberPrecision, boolean fractionGreaterOrEqual) throws VdeRuntimeException {

		final BigDecimal zero = new BigDecimal(0.0);
		BigDecimal[] array = getDecimalDivisionParts(value, numberPrecision);
		BigDecimal precision = array[0];
		BigDecimal integerPart = array[1];
		BigDecimal fractionPart = array[2];

		BigDecimal result = value;
		if (fractionPart.compareTo(zero) != 0.0) {
			BigDecimal floor = integerPart.multiply(precision);
			BigDecimal ceil = integerPart.add(new BigDecimal(1.0)).multiply(precision);

			// >=
			if (fractionGreaterOrEqual) {
				result = (fractionPart.compareTo(new BigDecimal(0.5)) != -1.0) ? ceil : floor;
			} else { // >
				result = (fractionPart.compareTo(new BigDecimal(0.5)) == 1.0) ? ceil : floor;
			}

		}

		return result;
	}

	/**
	 * identifies all parts and components needed for floor, ceil and round
	 * operation for a decimal value and precision of any Number type
	 * 
	 */
	private static BigDecimal[] getDecimalDivisionParts(BigDecimal value, Number numberPrecision) throws VdeRuntimeException {

		BigDecimal precision = new BigDecimal(numberPrecision.toString());
		final BigDecimal zero = new BigDecimal(0.0);

		if (precision.compareTo(zero) == -1.0) {
			throw new VdeRuntimeException("Precision can not be a negative number");
		}

		// TODO verify ...
		BigDecimal divisionResult = null;
		try {
			divisionResult = value.divide(precision, MathContext.UNLIMITED);
		} catch (ArithmeticException e) {
			divisionResult = value.divide(precision, MathContext.DECIMAL128);
		}

		BigDecimal integerPart = new BigDecimal(divisionResult.longValue());
		BigDecimal fractionPart = divisionResult.subtract(integerPart);

		BigDecimal[] array = new BigDecimal[3];
		array[0] = precision;
		array[1] = integerPart;
		array[2] = fractionPart;

		return array;
	}

	/**
	 * compute floor value of a number with respect to any type for precision
	 * 
	 */
	public static Number getFloor(Number numberValue, Number numberPrecision) throws VdeRuntimeException {

		Number[] array = getDivisionParts(numberValue, numberPrecision);
		Double value = array[0].doubleValue();
		Double precision = array[1].doubleValue();
		Long integerPart = array[2].longValue();
		Double fractionPart = array[3].doubleValue();

		Double result = value;
		if (fractionPart != 0.0) {
			result = integerPart * precision;
		}

		return result;
	}

	/**
	 * compute ceil value of a number with respect to any type for precision
	 * 
	 */
	public static Number getCeil(Number numberValue, Number numberPrecision) throws VdeRuntimeException {

		Number[] array = getDivisionParts(numberValue, numberPrecision);
		Double value = array[0].doubleValue();
		Double precision = array[1].doubleValue();
		Long integerPart = array[2].longValue();
		Double fractionPart = array[3].doubleValue();

		Double result = value;
		if (fractionPart != 0.0) {
			result = (integerPart + 1.0) * precision;
		}

		return result;
	}

	/**
	 * compute round value of a number with respect to any type for precision
	 * 
	 */
	public static Number getRound(Number numberValue, Number numberPrecision) throws VdeRuntimeException {

		return getRound(numberValue, numberPrecision, true);
	}

	/**
	 * compute round value of a number (non decimal) with respect to any type or precision,
	 * the boolean value is used to adjust for negative numbers rounding
	 * 
	 */
	public static Number getRound(Number numberValue, Number numberPrecision, boolean fractionGreaterOrEqual) throws VdeRuntimeException {

		Number[] array = getDivisionParts(numberValue, numberPrecision);
		Double value = array[0].doubleValue();
		Double precision = array[1].doubleValue();
		Long integerPart = array[2].longValue();
		Double fractionPart = array[3].doubleValue();

		Double result = value;
		if (fractionPart != 0.0) {
			Double floor = integerPart * precision;
			Double ceil = (integerPart + 1.0) * precision;
			// check if the comparison with fraction would be > or >=, this is
			// because in case of -ve numbers the '=' is not needed
			// Math.round(-2.5); => -2 and not -3
			result = fractionGreaterOrEqual ? ((fractionPart >= 0.5) ? ceil : floor) : ((fractionPart > 0.5) ? ceil : floor);
		}

		return result;
	}

	/**
	 * identifies all parts and components needed for floor, ceil and round
	 * operation for any number value (not decimal) and precision of any Number
	 * type
	 * 
	 */
	private static Number[] getDivisionParts(Number numberValue, Number numberPrecision) throws VdeRuntimeException {

		Double value = numberValue.doubleValue();
		Double precision = numberPrecision.doubleValue();
		if (precision < 0.0) {
			throw new VdeRuntimeException("Precision can not be a negative number");
		}

		Double divisionResult = (value / precision);
		Long integerPart = divisionResult.longValue();
		Double fractionPart = divisionResult - integerPart;

		Number[] array = new Number[4];
		array[0] = value;
		array[1] = precision;
		array[2] = integerPart;
		array[3] = fractionPart;

		return array;
	}

}
