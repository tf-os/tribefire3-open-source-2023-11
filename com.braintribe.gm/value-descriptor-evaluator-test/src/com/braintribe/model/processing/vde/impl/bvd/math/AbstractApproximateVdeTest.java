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
import java.util.Date;

import com.braintribe.model.bvd.math.ApproximateOperation;
import com.braintribe.model.processing.vde.impl.VDGenerator;
import com.braintribe.model.processing.vde.test.VdeTest;

public abstract class AbstractApproximateVdeTest extends VdeTest {

	public static VDGenerator $ = new VDGenerator(); 
	
	private void validateResult(Object result, Object expectedResult, Class<?> clazz){
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(clazz);
		assertThat(result).isEqualTo(expectedResult);
	}
	
	protected void validateIntegerResult(Object result, Object expectedResult) {
		validateResult(result, expectedResult, Integer.class);
	}

	protected void validateLongResult(Object result, Object expectedResult) {
		validateResult(result, expectedResult, Long.class);
	}

	protected void validateFloatResult(Object result, Object expectedResult) {
		validateResult(result, expectedResult, Float.class);
	}

	protected void validateDoubleResult(Object result, Object expectedResult) {
		validateResult(result, expectedResult, Double.class);
	}

	protected void validateDecimalResult(Object result, Object expectedResult) {
		validateResult(result, expectedResult, BigDecimal.class);
	}

	protected void testNullEmptyOperands(ApproximateOperation operation) throws Exception {
		evaluate(operation);
	}
	
	
	protected Date getSampleDate(){
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 7);
		cal.set(Calendar.HOUR_OF_DAY, 15);
		cal.set(Calendar.MINUTE, 33);
		cal.set(Calendar.SECOND, 11);
		cal.set(Calendar.MILLISECOND, 39);
		
		return cal.getTime();
	}
	
	private void validateDate(Calendar result, Calendar expected){
		validateCalendarEntry(Calendar.YEAR, result, expected);
		validateCalendarEntry(Calendar.MONTH, result, expected);
		validateCalendarEntry(Calendar.DAY_OF_MONTH, result, expected);
		validateCalendarEntry(Calendar.HOUR_OF_DAY, result, expected);
		validateCalendarEntry(Calendar.MINUTE, result, expected);
		validateCalendarEntry(Calendar.SECOND, result, expected);
		validateCalendarEntry(Calendar.MILLISECOND, result, expected);
	}
	private void validateCalendarEntry(int field,Calendar result, Calendar expected){
		assertThat(result.get(field)).isEqualTo(expected.get(field));
	}
	
	protected void validateDate(Object result, Date expected){
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Date.class);
		validateDate((Date)result, expected);
	}
	
	private void validateDate(Date result, Date expected){
		Calendar calResult = Calendar.getInstance();
		calResult.setTime(result);
		Calendar calExpected = Calendar.getInstance();
		calExpected.setTime(expected);
		validateDate(calResult, calExpected);
	}
}
