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

import com.braintribe.model.bvd.predicate.GreaterOrEqual;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.impl.misc.Name;
import com.braintribe.model.processing.vde.impl.misc.SalaryRange;
import com.braintribe.model.processing.vde.impl.misc.SizeRange;

public class GreaterOrEqualVdeTest extends AbstractPredicateVdeTest {

	@Test
	public void testObjectComparableGreaterOrEqual() throws Exception {

		GreaterOrEqual predicate = $.greaterOrEqual();
		Name n1 = Name.T.create();
		n1.setFirst("c");
		Name n2 = Name.T.create();
		n2.setFirst("c");

		predicate.setLeftOperand(n1);
		predicate.setRightOperand(n2);

		Object result = evaluate(predicate);
		validatePositiveResult(result);

		n2.setFirst("w");

		result = evaluate(predicate);
		validateNegativeResult(result);

	}

	@Test(expected = VdeRuntimeException.class)
	public void testEnumDifferentTypeGreaterOrEqual() throws Exception {

		GreaterOrEqual predicate = $.greaterOrEqual();
		predicate.setLeftOperand(SizeRange.medium);
		predicate.setRightOperand(SalaryRange.medium);

		evaluate(predicate);

	}

	@Test
	public void testEnumGreaterOrEqual() throws Exception {

		GreaterOrEqual predicate = $.greaterOrEqual();
		predicate.setLeftOperand(SalaryRange.medium);
		predicate.setRightOperand(SalaryRange.low);

		Object result = evaluate(predicate);
		validatePositiveResult(result);

		predicate.setRightOperand(SalaryRange.medium);
		result = evaluate(predicate);
		validatePositiveResult(result);

		predicate.setRightOperand(SalaryRange.high);
		result = evaluate(predicate);
		validateNegativeResult(result);

	}

	@Test
	public void testStringGreaterOrEqual() throws Exception {

		GreaterOrEqual predicate = $.greaterOrEqual();
		predicate.setLeftOperand(new String("abcd"));
		predicate.setRightOperand(new String("abc"));

		Object result = evaluate(predicate);
		validatePositiveResult(result);

		predicate.setRightOperand(new String("abcd"));
		result = evaluate(predicate);
		validatePositiveResult(result);

		predicate.setRightOperand(new String("dcba"));
		result = evaluate(predicate);
		validateNegativeResult(result);

	}

	@Test
	public void testDateGreaterOrEqual() throws Exception {
		Calendar cal = Calendar.getInstance();

		Date date = cal.getTime();
		cal.set(Calendar.YEAR, 2011);
		Date otherDate = cal.getTime();

		GreaterOrEqual predicate = $.greaterOrEqual();
		predicate.setLeftOperand(date);
		predicate.setRightOperand(otherDate);

		Object result = evaluate(predicate);
		validatePositiveResult(result);

		predicate.setRightOperand(date);
		result = evaluate(predicate);
		validatePositiveResult(result);

		cal.set(Calendar.YEAR, 3010);
		Date anotherDate = cal.getTime();
		predicate.setRightOperand(anotherDate);

		result = evaluate(predicate);
		validateNegativeResult(result);

	}

	@Test
	public void testIntegerGreaterOrEqual() throws Exception {

		GreaterOrEqual predicate = $.greaterOrEqual();
		predicate.setLeftOperand(new Integer(3));
		predicate.setRightOperand(new Integer(2));

		Object result = evaluate(predicate);
		validatePositiveResult(result);

		predicate.setRightOperand(new Integer(3));
		result = evaluate(predicate);
		validatePositiveResult(result);

		predicate.setRightOperand(new Integer(4));
		result = evaluate(predicate);
		validateNegativeResult(result);

	}

	@Test
	public void testLongGreaterOrEqual() throws Exception {

		GreaterOrEqual predicate = $.greaterOrEqual();

		predicate.setLeftOperand(new Long(3));
		List<Object> operandsList = getLongOperandsGreaterOrEqual();
		for (Object object : operandsList) {
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}

		// commutative
		predicate.setRightOperand(new Long(4));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}

		// equal
		predicate.setRightOperand(new Long(2));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}

	@Test
	public void testFloatGreaterOrEqual() throws Exception {

		GreaterOrEqual predicate = $.greaterOrEqual();

		predicate.setLeftOperand(new Float(3));
		List<Object> operandsList = getFloatOperandsGreaterOrEqual();
		for (Object object : operandsList) {
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}

		// commutative
		predicate.setRightOperand(new Float(4));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}

		// equal
		predicate.setRightOperand(new Float(2));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}

	@Test
	public void testDoubleGreaterOrEqual() throws Exception {

		GreaterOrEqual predicate = $.greaterOrEqual();

		predicate.setLeftOperand(new Double(3));
		List<Object> operandsList = getDoubleOperandsGreaterOrEqual();
		for (Object object : operandsList) {
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}

		// commutative
		predicate.setRightOperand(new Double(4));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}

		// equal
		predicate.setRightOperand(new Double(2));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}

	@Test
	public void testDecimalGreaterOrEqual() throws Exception {

		GreaterOrEqual predicate = $.greaterOrEqual();

		predicate.setLeftOperand(new BigDecimal(3));
		List<Object> operandsList = getDecimalOperandsGreaterOrEqual();
		for (Object object : operandsList) {
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}

		// commutative
		predicate.setRightOperand(new BigDecimal(4));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}

		// equal
		predicate.setRightOperand(new BigDecimal(2));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}

	private List<Object> getDecimalOperandsGreaterOrEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new BigDecimal(2));
		result.add(new Double(2));
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getDoubleOperandsGreaterOrEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Double(2));
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getFloatOperandsGreaterOrEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getLongOperandsGreaterOrEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

}
