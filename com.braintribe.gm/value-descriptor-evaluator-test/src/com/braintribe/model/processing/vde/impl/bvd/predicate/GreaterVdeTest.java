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

import com.braintribe.model.bvd.predicate.Greater;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.impl.misc.Child;
import com.braintribe.model.processing.vde.impl.misc.Name;
import com.braintribe.model.processing.vde.impl.misc.Person;
import com.braintribe.model.processing.vde.impl.misc.SalaryRange;
import com.braintribe.model.processing.vde.impl.misc.SizeRange;

public class GreaterVdeTest extends AbstractPredicateVdeTest {

	
	@Test 
	public void testObjectComparableGreater() throws Exception {

		Greater predicate = $.greater();
		Name n1= Name.T.create();
		n1.setFirst("c");
		Name n2= Name.T.create();
		n2.setFirst("b");
		
		predicate.setLeftOperand(n1);
		predicate.setRightOperand(n2);

		Object result = evaluate(predicate);
		validatePositiveResult(result);
		
		n2.setFirst("d");
		
		result = evaluate(predicate);
		validateNegativeResult(result);
	}
	
	@Test (expected= VdeRuntimeException.class)
	public void testObjectNotComparableToEachOtherGreater() throws Exception {

		Greater predicate = $.greater();
		Name n= Name.T.create();
		n.setFirst("c");
		Person p= Person.T.create();

		predicate.setLeftOperand(n);
		predicate.setRightOperand(p);

		evaluate(predicate);
	}
	
	@Test (expected= VdeRuntimeException.class)
	public void testObjectNotComparableGreater() throws Exception {

		Greater predicate = $.greater();
		Name n= Name.T.create();
		n.setFirst("c");
		Child c= Child.T.create();

		predicate.setLeftOperand(n);
		predicate.setRightOperand(c);

		evaluate(predicate);
	}
	
	@Test (expected= VdeRuntimeException.class)
	public void testEnumDifferentTypeGreater() throws Exception {

		Greater predicate = $.greater();
		predicate.setLeftOperand(SizeRange.medium);
		predicate.setRightOperand(SalaryRange.medium);

		evaluate(predicate);
		
	}
	
	@Test
	public void testEnumGreater() throws Exception {

		Greater predicate = $.greater();
		predicate.setLeftOperand(SalaryRange.medium);
		predicate.setRightOperand(SalaryRange.low);

		Object result = evaluate(predicate);
		validatePositiveResult(result);
		
		predicate.setRightOperand(SalaryRange.high);

		result = evaluate(predicate);
		validateNegativeResult(result);
		
	}
	
	@Test
	public void testStringGreater() throws Exception {

		Greater predicate = $.greater();
		predicate.setLeftOperand(new String("abcd"));
		predicate.setRightOperand(new String("abc"));

		Object result = evaluate(predicate);
		validatePositiveResult(result);
		
		predicate.setRightOperand(new String("dcba"));

		result = evaluate(predicate);
		validateNegativeResult(result);
		
	}
	
	@Test
	public void testDateGreater() throws Exception {
		Calendar cal = Calendar.getInstance();
		
		Date date = cal.getTime();
		cal.set(Calendar.YEAR, 2011);
		Date otherDate = cal.getTime();
		
		Greater predicate = $.greater();
		predicate.setLeftOperand(date);
		predicate.setRightOperand(otherDate);

		Object result = evaluate(predicate);
		validatePositiveResult(result);
		
		cal.set(Calendar.YEAR, 3010);
		Date anotherDate = cal.getTime();
		predicate.setRightOperand(anotherDate);

		result = evaluate(predicate);
		validateNegativeResult(result);
		
	}

	@Test
	public void testIntegerGreater() throws Exception {

		Greater predicate = $.greater();
		predicate.setLeftOperand(new Integer(3));
		predicate.setRightOperand(new Integer(2));

		Object result = evaluate(predicate);
		validatePositiveResult(result);
		
		predicate.setRightOperand(new Integer(4));

		result = evaluate(predicate);
		validateNegativeResult(result);
		
	}
	
	@Test
	public void testLongGreater() throws Exception {

		Greater predicate = $.greater();
		
		predicate.setLeftOperand(new Long(3));
		List<Object> operandsList = getLongOperandsGreater();
		for(Object object : operandsList){
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
		
		// commutative
		predicate.setRightOperand(new Long(4));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}		
	}
	
	@Test
	public void testFloatGreater() throws Exception {

		Greater predicate = $.greater();
		
		predicate.setLeftOperand(new Float(3));
		List<Object> operandsList = getFloatOperandsGreater();
		for(Object object : operandsList){
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
		
		// commutative
		predicate.setRightOperand(new Float(4));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}		
	}
	
	@Test
	public void testDoubleGreater() throws Exception {

		Greater predicate = $.greater();
		
		predicate.setLeftOperand(new Double(3));
		List<Object> operandsList = getDoubleOperandsGreater();
		for(Object object : operandsList){
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
		
		// commutative
		predicate.setRightOperand(new Double(4));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}		
	}
	
	@Test
	public void testDecimalGreater() throws Exception {

		Greater predicate = $.greater();
		
		predicate.setLeftOperand(new BigDecimal(3));
		List<Object> operandsList = getDecimalOperandsGreater();
		for(Object object : operandsList){
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
		
		// commutative
		predicate.setRightOperand(new BigDecimal(4));
		for(Object object : operandsList){
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}		
	}
	
	private List<Object> getDecimalOperandsGreater() {
		List<Object> result = new ArrayList<Object>();
		result.add(new BigDecimal(2));
		result.add(new Double(2));
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}	
	
	private List<Object> getDoubleOperandsGreater() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Double(2));
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}	
	
	private List<Object> getFloatOperandsGreater() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}	
	
	private List<Object> getLongOperandsGreater() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}	
	
}
