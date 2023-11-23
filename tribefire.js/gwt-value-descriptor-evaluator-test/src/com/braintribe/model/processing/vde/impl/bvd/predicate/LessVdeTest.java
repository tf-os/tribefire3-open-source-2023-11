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

import com.braintribe.model.bvd.predicate.Less;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.impl.misc.Name;
import com.braintribe.model.processing.vde.impl.misc.SalaryRange;
import com.braintribe.model.processing.vde.impl.misc.SizeRange;

public class LessVdeTest extends AbstractPredicateVdeTest {

	@Test
	public void testObjectComparableLess() throws Exception {

		Less predicate = $.less();
		Name n1 = Name.T.create();
		n1.setFirst("c");
		Name n2 = Name.T.create();
		n2.setFirst("b");

		predicate.setLeftOperand(n1);
		predicate.setRightOperand(n2);

		Object result = evaluate(predicate);
		validateNegativeResult(result);

		n2.setFirst("d");

		result = evaluate(predicate);
		validatePositiveResult(result);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testEnumDifferentTypeLess() throws Exception {

		Less predicate = $.less();
		predicate.setLeftOperand(SizeRange.medium);
		predicate.setRightOperand(SalaryRange.medium);

		evaluate(predicate);

	}

	@Test
	public void testEnumLess() throws Exception {

		Less predicate = $.less();
		predicate.setLeftOperand(SalaryRange.medium);
		predicate.setRightOperand(SalaryRange.low);

		Object result = evaluate(predicate);
		validateNegativeResult(result);

		predicate.setRightOperand(SalaryRange.high);

		result = evaluate(predicate);
		validatePositiveResult(result);

	}

	@Test
	public void testStringLess() throws Exception {

		Less predicate = $.less();
		predicate.setLeftOperand(new String("abcd"));
		predicate.setRightOperand(new String("abc"));

		Object result = evaluate(predicate);
		validateNegativeResult(result);

		predicate.setRightOperand(new String("dcba"));

		result = evaluate(predicate);
		validatePositiveResult(result);

	}

	@Test
	public void testDateLess() throws Exception {
		Calendar cal = Calendar.getInstance();

		Date date = cal.getTime();
		cal.set(Calendar.YEAR, 2011);
		Date otherDate = cal.getTime();

		Less predicate = $.less();
		predicate.setLeftOperand(date);
		predicate.setRightOperand(otherDate);

		Object result = evaluate(predicate);
		validateNegativeResult(result);

		cal.set(Calendar.YEAR, 3010);
		Date anotherDate = cal.getTime();
		predicate.setRightOperand(anotherDate);

		result = evaluate(predicate);
		validatePositiveResult(result);

	}

	@Test
	public void testIntegerLess() throws Exception {

		Less predicate = $.less();
		predicate.setLeftOperand(new Integer(3));
		predicate.setRightOperand(new Integer(2));

		Object result = evaluate(predicate);
		validateNegativeResult(result);

		predicate.setRightOperand(new Integer(4));

		result = evaluate(predicate);
		validatePositiveResult(result);

	}

	@Test
	public void testLongLess() throws Exception {

		Less predicate = $.less();

		predicate.setLeftOperand(new Long(3));
		List<Object> operandsList = getLongOperandsLess();
		for (Object object : operandsList) {
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}

		// commutative
		predicate.setRightOperand(new Long(4));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}

	@Test
	public void testFloatLess() throws Exception {

		Less predicate = $.less();

		predicate.setLeftOperand(new Float(3));
		List<Object> operandsList = getFloatOperandsLess();
		for (Object object : operandsList) {
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}

		// commutative
		predicate.setRightOperand(new Float(4));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}

	@Test
	public void testDoubleLess() throws Exception {

		Less predicate = $.less();

		predicate.setLeftOperand(new Double(3));
		List<Object> operandsList = getDoubleOperandsLess();
		for (Object object : operandsList) {
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}

		// commutative
		predicate.setRightOperand(new Double(4));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}

	@Test
	public void testDecimalLess() throws Exception {

		Less predicate = $.less();

		predicate.setLeftOperand(new BigDecimal(3));
		List<Object> operandsList = getDecimalOperandsLess();
		for (Object object : operandsList) {
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}

		// commutative
		predicate.setRightOperand(new BigDecimal(4));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}

	private List<Object> getDecimalOperandsLess() {
		List<Object> result = new ArrayList<Object>();
		result.add(new BigDecimal(2));
		result.add(new Double(2));
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getDoubleOperandsLess() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Double(2));
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getFloatOperandsLess() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getLongOperandsLess() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

}
