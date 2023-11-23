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

import com.braintribe.model.bvd.predicate.NotEqual;
import com.braintribe.model.processing.vde.impl.misc.Name;
import com.braintribe.model.processing.vde.impl.misc.SalaryRange;

public class NotEqualVdeTest extends AbstractPredicateVdeTest {

	@Test
	public void testObjectComparableNotEqual() throws Exception {

		NotEqual predicate = $.notEqual();
		Name n1 = Name.T.create();
		n1.setFirst("c");
		Name n2 = Name.T.create();
		n2.setFirst("c");

		predicate.setLeftOperand(n1);
		predicate.setRightOperand(n2);

		Object result = evaluate(predicate);
		validateNegativeResult(result);

		n2.setFirst("w");

		result = evaluate(predicate);
		validatePositiveResult(result);

	}

	@Test
	public void testEnumNotEqual() throws Exception {

		NotEqual predicate = $.notEqual();
		predicate.setLeftOperand(SalaryRange.medium);
		predicate.setRightOperand(SalaryRange.medium);

		Object result = evaluate(predicate);
		validateNegativeResult(result);

		predicate.setRightOperand(SalaryRange.high);

		result = evaluate(predicate);
		validatePositiveResult(result);

	}

	@Test
	public void testStringNotEqual() throws Exception {

		NotEqual predicate = $.notEqual();
		predicate.setLeftOperand(new String("abcd"));
		predicate.setRightOperand(new String("abcd"));

		Object result = evaluate(predicate);
		validateNegativeResult(result);

		predicate.setRightOperand(new String("dcba"));

		result = evaluate(predicate);
		validatePositiveResult(result);

	}

	@Test
	public void testDateNotEqual() throws Exception {
		Calendar cal = Calendar.getInstance();

		Date date = cal.getTime();
		NotEqual predicate = $.notEqual();
		predicate.setLeftOperand(date);
		predicate.setRightOperand(date);

		Object result = evaluate(predicate);
		validateNegativeResult(result);

		cal.set(Calendar.YEAR, 2010);
		Date otherDate = cal.getTime();
		predicate.setRightOperand(otherDate);

		result = evaluate(predicate);
		validatePositiveResult(result);

	}

	@Test
	public void testBooleanNotEqual() throws Exception {

		NotEqual predicate = $.notEqual();
		predicate.setLeftOperand(new Boolean(true));
		predicate.setRightOperand(new Boolean(true));

		Object result = evaluate(predicate);
		validateNegativeResult(result);

		predicate.setRightOperand(new Boolean(false));

		result = evaluate(predicate);
		validatePositiveResult(result);

	}

	@Test
	public void testIntegerNotEqual() throws Exception {

		NotEqual predicate = $.notEqual();
		predicate.setLeftOperand(new Integer(2));
		predicate.setRightOperand(new Integer(2));

		Object result = evaluate(predicate);
		validateNegativeResult(result);

		predicate.setRightOperand(new Integer(4));

		result = evaluate(predicate);
		validatePositiveResult(result);

	}

	@Test
	public void testLongNotEqual() throws Exception {

		NotEqual predicate = $.notEqual();

		predicate.setLeftOperand(new Long(2));
		List<Object> operandsList = getLongOperandsNotEqual();
		for (Object object : operandsList) {
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}

		// commutative
		predicate.setRightOperand(new Long(3));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}

	@Test
	public void testFloatNotEqual() throws Exception {

		NotEqual predicate = $.notEqual();

		predicate.setLeftOperand(new Float(2));
		List<Object> operandsList = getFloatOperandsNotEqual();
		for (Object object : operandsList) {
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}

		// commutative
		predicate.setRightOperand(new Float(3));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}

	@Test
	public void testDoubleNotEqual() throws Exception {

		NotEqual predicate = $.notEqual();

		predicate.setLeftOperand(new Double(2));
		List<Object> operandsList = getDoubleOperandsNotEqual();
		for (Object object : operandsList) {
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}

		// commutative
		predicate.setRightOperand(new Double(3));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}

	@Test
	public void testDecimalNotEqual() throws Exception {

		NotEqual predicate = $.notEqual();

		predicate.setLeftOperand(new BigDecimal(2));
		List<Object> operandsList = getDecimalOperandsNotEqual();
		for (Object object : operandsList) {
			predicate.setRightOperand(object);
			Object result = evaluate(predicate);
			validateNegativeResult(result);
		}

		// commutative
		predicate.setRightOperand(new BigDecimal(3));
		for (Object object : operandsList) {
			predicate.setLeftOperand(object);
			Object result = evaluate(predicate);
			validatePositiveResult(result);
		}
	}

	private List<Object> getDecimalOperandsNotEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new BigDecimal(2));
		result.add(new Double(2));
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getDoubleOperandsNotEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Double(2));
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getFloatOperandsNotEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Float(2));
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

	private List<Object> getLongOperandsNotEqual() {
		List<Object> result = new ArrayList<Object>();
		result.add(new Long(2));
		result.add(new Integer(2));
		return result;
	}

}
