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
package com.braintribe.model.processing.vde.impl.bvd.predicate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.braintribe.model.bvd.predicate.LessOrEqual;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.impl.misc.Name;
import com.braintribe.model.processing.vde.impl.misc.SalaryRange;
import com.braintribe.model.processing.vde.impl.misc.SizeRange;

public class LessOrEqualVdeTest extends AbstractPredicateVdeTest {

	@Test 
	public void testObjectComparableLessOrEqual() throws Exception {

		LessOrEqual predicate = $.lessOrEqual();
		Name n1= Name.T.create();
		n1.setFirst("c");
		Name n2= Name.T.create();
		n2.setFirst("b");
		
		predicate.setLeftOperand(n1);
		predicate.setRightOperand(n2);

		Object result = evaluate(predicate);
		validateNegativeResult(result);
		
		n2.setFirst("d");
		
		result = evaluate(predicate);
		validatePositiveResult(result);
	}
	
	@Test (expected= VdeRuntimeException.class)
	public void testEnumDifferentTypeLessOrEqual() throws Exception {

		LessOrEqual predicate = $.lessOrEqual();
		predicate.setLeftOperand(SizeRange.medium);
		predicate.setRightOperand(SalaryRange.medium);

		evaluate(predicate);
		
	}
	
	@Test
	public void testEnumLessOrEqual() throws Exception {

		LessOrEqual predicate = $.lessOrEqual();
		predicate.setLeftOperand(SalaryRange.medium);
		predicate.setRightOperand(SalaryRange.low);

		Object result = evaluate(predicate);
		validateNegativeResult(result);
		
		predicate.setRightOperand(SalaryRange.medium);
		result = evaluate(predicate);
		validatePositiveResult(result);
		
		predicate.setRightOperand(SalaryRange.high);
		result = evaluate(predicate);
		validatePositiveResult(result);
		
	}
	
	@Test
	public void testStringLessOrEqual() throws Exception {

		LessOrEqual predicate = $.lessOrEqual();
		predicate.setLeftOperand(new String("abcd"));
		predicate.setRightOperand(new String("abc"));

		Object result = evaluate(predicate);
		validateNegativeResult(result);
		
		predicate.setRightOperand(new String("abcd"));
		result = evaluate(predicate);
		validatePositiveResult(result);
		
		predicate.setRightOperand(new String("dcba"));
		result = evaluate(predicate);
		validatePositiveResult(result);
		
	}
	
	@Test
	public void testDateLessOrEqual() throws Exception {
		Calendar cal = Calendar.getInstance();
		
		Date date = cal.getTime();
		cal.set(Calendar.YEAR, 2011);
		Date otherDate = cal.getTime();
		
		LessOrEqual predicate = $.lessOrEqual();
		predicate.setLeftOperand(date);
		predicate.setRightOperand(otherDate);

		Object result = evaluate(predicate);
		validateNegativeResult(result);
		
		predicate.setRightOperand(date);
		result = evaluate(predicate);
		validatePositiveResult(result);
		
		cal.set(Calendar.YEAR, 3010);
		Date anotherDate = cal.getTime();
		predicate.setRightOperand(anotherDate);

		result = evaluate(predicate);
		validatePositiveResult(result);
		
	}

	@Test
	public void testIntegerLessOrEqual() throws Exception {

		LessOrEqual predicate = $.lessOrEqual();
		predicate.setLeftOperand(new Integer(3));
		predicate.setRightOperand(new Integer(2));

		Object result = evaluate(predicate);
		validateNegativeResult(result);
		
		predicate.setRightOperand(new Integer(3));
		result = evaluate(predicate);
		validatePositiveResult(result);
		
		predicate.setRightOperand(new Integer(4));
		result = evaluate(predicate);
		validatePositiveResult(result);
		
	}
	
	@Test
	public void testLongLessOrEqual() throws Exception {

		LessOrEqual predicate = $.lessOrEqual();
		
		predicate.setLeftOperand(new Long(3));
		List<Object> operandsList = getLongOperandsLessOrEqual();
		for(Object object : operandsList){
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}
		
		// commutative
		predicate.setRightOperand(new Long(4));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
		
		//equal
		predicate.setRightOperand(new Long(2));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}
	
	@Test
	public void testFloatLessOrEqual() throws Exception {

		LessOrEqual predicate = $.lessOrEqual();
		
		predicate.setLeftOperand(new Float(3));
		List<Object> operandsList = getFloatOperandsLessOrEqual();
		for(Object object : operandsList){
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}
		
		// commutative
		predicate.setRightOperand(new Float(4));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
		
		//equal
		predicate.setRightOperand(new Float(2));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}
	
	@Test
	public void testDoubleLessOrEqual() throws Exception {

		LessOrEqual predicate = $.lessOrEqual();
		
		predicate.setLeftOperand(new Double(3));
		List<Object> operandsList = getDoubleOperandsLessOrEqual();
		for(Object object : operandsList){
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}
		
		// commutative
		predicate.setRightOperand(new Double(4));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}	
		
		//equal
		predicate.setRightOperand(new Double(2));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}
	
	@Test
	public void testDecimalLessOrEqual() throws Exception {

		LessOrEqual predicate = $.lessOrEqual();
		
		predicate.setLeftOperand(new BigDecimal(3));
		List<Object> operandsList = getDecimalOperandsLessOrEqual();
		for(Object object : operandsList){
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}
		
		// commutative
		predicate.setRightOperand(new BigDecimal(4));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
		
		//equal
		predicate.setRightOperand(new BigDecimal(2));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}
	
	private List<Object> getDecimalOperandsLessOrEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new BigDecimal(2));
		result.add(new Double(2));
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}	
	
	private List<Object> getDoubleOperandsLessOrEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Double(2));
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}	
	
	private List<Object> getFloatOperandsLessOrEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}	
	
	private List<Object> getLongOperandsLessOrEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}	
	
}
