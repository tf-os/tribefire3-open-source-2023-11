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

import com.braintribe.model.bvd.predicate.Equal;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.impl.misc.Name;
import com.braintribe.model.processing.vde.impl.misc.Person;
import com.braintribe.model.processing.vde.impl.misc.SalaryRange;
import com.braintribe.model.processing.vde.impl.misc.SizeRange;

public class EqualVdeTest extends AbstractPredicateVdeTest {

	@Test 
	public void testObjectComparableEqual() throws Exception {

		Equal predicate = $.equal();
		Name n1= Name.T.create();
		n1.setFirst("c");
		Name n2= Name.T.create();
		n2.setFirst("c");
		
		predicate.setLeftOperand(n1);
		predicate.setRightOperand(n2);

		Object result = evaluate(predicate);
		validatePositiveResult(result);
		
		n2.setFirst("w");
		
		result = evaluate(predicate);
		validateNegativeResult(result);
		
	}
	
	
	@Test (expected= VdeRuntimeException.class)
	public void testNullRightOperandEqual() throws Exception {

		Equal predicate = $.equal();
		predicate.setLeftOperand(Person.T.create());

		evaluate(predicate);
		
	}
	
	@Test (expected= VdeRuntimeException.class)
	public void testNullLeftOperandEqual() throws Exception {

		Equal predicate = $.equal();
		predicate.setRightOperand(Person.T.create());

		evaluate(predicate);
		
	}
	
	
	@Test (expected= VdeRuntimeException.class)
	public void testRandomEqual() throws Exception {

		Equal predicate = $.equal();
		predicate.setLeftOperand(new Date());
		predicate.setRightOperand(Person.T.create());

		evaluate(predicate);
		
	}
	
	@Test
	public void testDateStringEqual() throws Exception {

		Equal predicate = $.equal();
		predicate.setLeftOperand(new Date());
		predicate.setRightOperand("date");

		Object result = evaluate(predicate);
		validateNegativeResult(result);
		
	}
	
	@Test
	public void testEnumDifferentTypeEqual() throws Exception {

		Equal predicate = $.equal();
		predicate.setLeftOperand(SizeRange.medium);
		predicate.setRightOperand(SalaryRange.medium);

		Object result = evaluate(predicate);
		validateNegativeResult(result);

	}
	
	@Test
	public void testEnumEqual() throws Exception {

		Equal predicate = $.equal();
		predicate.setLeftOperand(SalaryRange.medium);
		predicate.setRightOperand(SalaryRange.medium);

		Object result = evaluate(predicate);
		validatePositiveResult(result);
		
		predicate.setRightOperand(SalaryRange.high);

		result = evaluate(predicate);
		validateNegativeResult(result);
		
	}
	
	@Test
	public void testStringEqual() throws Exception {

		Equal predicate = $.equal();
		predicate.setLeftOperand(new String("abcd"));
		predicate.setRightOperand(new String("abcd"));

		Object result = evaluate(predicate);
		validatePositiveResult(result);
		
		predicate.setRightOperand(new String("dcba"));

		result = evaluate(predicate);
		validateNegativeResult(result);
		
	}
	
	@Test
	public void testDateEqual() throws Exception {
		Calendar cal = Calendar.getInstance();
		
		Date date = cal.getTime();
		Equal predicate = $.equal();
		predicate.setLeftOperand(date);
		predicate.setRightOperand(date);

		Object result = evaluate(predicate);
		validatePositiveResult(result);
		
		cal.set(Calendar.YEAR, 2010);
		Date otherDate = cal.getTime();
		predicate.setRightOperand(otherDate);

		result = evaluate(predicate);
		validateNegativeResult(result);
		
	}
	
	@Test
	public void testBooleanEqual() throws Exception {

		Equal predicate = $.equal();
		predicate.setLeftOperand(new Boolean(true));
		predicate.setRightOperand(new Boolean(true));

		Object result = evaluate(predicate);
		validatePositiveResult(result);
		
		predicate.setRightOperand(new Boolean(false));

		result = evaluate(predicate);
		validateNegativeResult(result);
		
	}

	@Test
	public void testIntegerEqual() throws Exception {

		Equal predicate = $.equal();
		predicate.setLeftOperand(new Integer(2));
		predicate.setRightOperand(new Integer(2));

		Object result = evaluate(predicate);
		validatePositiveResult(result);
		
		predicate.setRightOperand(new Integer(4));

		result = evaluate(predicate);
		validateNegativeResult(result);
		
	}
	
	@Test
	public void testLongEqual() throws Exception {

		Equal predicate = $.equal();
		
		predicate.setLeftOperand(new Long(2));
		List<Object> operandsList = getLongOperandsEqual();
		for(Object object : operandsList){
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
		
		// commutative
		predicate.setRightOperand(new Long(3));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}		
	}
	
	@Test
	public void testFloatEqual() throws Exception {

		Equal predicate = $.equal();
		
		predicate.setLeftOperand(new Float(2));
		List<Object> operandsList = getFloatOperandsEqual();
		for(Object object : operandsList){
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
		
		// commutative
		predicate.setRightOperand(new Float(3));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}		
	}
	
	@Test
	public void testDoubleEqual() throws Exception {

		Equal predicate = $.equal();
		
		predicate.setLeftOperand(new Double(2));
		List<Object> operandsList = getDoubleOperandsEqual();
		for(Object object : operandsList){
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
		
		// commutative
		predicate.setRightOperand(new Double(3));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}		
	}
	
	@Test
	public void testDecimalEqual() throws Exception {

		Equal predicate = $.equal();
		
		predicate.setLeftOperand(new BigDecimal(2));
		List<Object> operandsList = getDecimalOperandsEqual();
		for(Object object : operandsList){
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
		
		// commutative
		predicate.setRightOperand(new BigDecimal(3));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}		
	}
	
	private List<Object> getDecimalOperandsEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new BigDecimal(2));
		result.add(new Double(2));
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}	
	
	private List<Object> getDoubleOperandsEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Double(2));
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}	
	
	private List<Object> getFloatOperandsEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}	
	
	private List<Object> getLongOperandsEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}	
	
}
