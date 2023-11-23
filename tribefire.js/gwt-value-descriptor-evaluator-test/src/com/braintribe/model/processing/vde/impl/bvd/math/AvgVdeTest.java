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
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.braintribe.model.bvd.math.Avg;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.AvgVde;

/**
 * Provides tests for {@link AvgVde}.
 * 
 */
public class AvgVdeTest extends AbstractArithmeticVdeTest {

	@Test(expected = VdeRuntimeException.class)
	public void testNullOperandsAvg() throws Exception {

		Avg math = $.avg();

		testNullEmptyOperands(math);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testEmptyOperandsAvg() throws Exception {

		Avg math = $.avg();
		List<Object> operands = new ArrayList<Object>();
		math.setOperands(operands);

		testNullEmptyOperands(math);
	}

	@Test
	public void testSingleOperandAvg() throws Exception {

		Avg math = $.avg();
		List<Object> operands = new ArrayList<Object>();
		operands.add(new Double(3));
		math.setOperands(operands);

		testSingleOperand(math);
	}

	@Test
	public void testIntegerAvg() throws Exception {

		Avg math = $.avg();
		List<Object> operands = new ArrayList<Object>();
		operands.add(new Integer(2));
		operands.add(new Integer(3));
		operands.add(new Integer(1));
		math.setOperands(operands);

		Object result = evaluate(math);
		validateDoubleResult(result, new Double(2));
	}

	@Test
	public void testLongAvg() throws Exception {

		Avg math = $.avg();
		List<List<Object>> operandsAvg = getLongOperandsAvg();

		for (List<Object> operandList : operandsAvg) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDoubleResult(result, new Double(3));
		}

		operandsAvg = getLongOperandsAvg();
		Collections.reverse(operandsAvg);

		for (List<Object> operandList : operandsAvg) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDoubleResult(result, new Double(3));
		}
	}

	@Test
	public void testFloatAvg() throws Exception {

		Avg math = $.avg();
		List<List<Object>> operandsAvg = getFloatOperandsAvg();

		for (List<Object> operandList : operandsAvg) {
			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDoubleResult(result, new Double(3));
		}

		operandsAvg = getFloatOperandsAvg();
		Collections.reverse(operandsAvg);

		for (List<Object> operandList : operandsAvg) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDoubleResult(result, new Double(3));
		}
	}

	@Test
	public void testDoubleAvg() throws Exception {

		Avg math = $.avg();
		List<List<Object>> operandsAvg = getDoubleOperandsAvg();

		for (List<Object> operandList : operandsAvg) {
			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDoubleResult(result, new Double(3));
		}

		operandsAvg = getDoubleOperandsAvg();
		Collections.reverse(operandsAvg);

		for (List<Object> operandList : operandsAvg) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDoubleResult(result, new Double(3));
		}
	}

	@Test
	public void testDecimalAvg() throws Exception {
		Avg math = $.avg();
		List<List<Object>> operandsAvg = getDecimalOperandsAvg();

		for (List<Object> operandList : operandsAvg) {
			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDecimalResult(result, new BigDecimal(3));
		}

		operandsAvg = getDecimalOperandsAvg();
		Collections.reverse(operandsAvg);

		for (List<Object> operandList : operandsAvg) {

			math.setOperands(operandList);
			Object result = evaluate(math);
			validateDecimalResult(result, new BigDecimal(3));
		}
	}

	private List<List<Object>> getLongOperandsAvg() {
		List<List<Object>> result = new ArrayList<List<Object>>();

		List<Object> list = new ArrayList<Object>();
		list.add(new Long(2));
		list.add(new Long(3));
		list.add(new Long(4));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Long(2));
		list.add(new Integer(3));
		list.add(new Long(4));
		result.add(list);

		return result;
	}

	private List<List<Object>> getFloatOperandsAvg() {
		List<List<Object>> result = new ArrayList<List<Object>>();

		List<Object> list = new ArrayList<Object>();
		list.add(new Float(2));
		list.add(new Float(3));
		list.add(new Float(4));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Float(2));
		list.add(new Integer(3));
		list.add(new Float(4));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Float(2));
		list.add(new Long(3));
		list.add(new Float(4));
		result.add(list);

		return result;
	}

	private List<List<Object>> getDoubleOperandsAvg() {
		List<List<Object>> result = new ArrayList<List<Object>>();

		List<Object> list = new ArrayList<Object>();
		list.add(new Double(2));
		list.add(new Double(3));
		list.add(new Double(4));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Double(2));
		list.add(new Integer(3));
		list.add(new Double(4));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Double(2));
		list.add(new Long(3));
		list.add(new Double(4));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new Double(2));
		list.add(new Float(3));
		list.add(new Double(4));
		result.add(list);

		return result;
	}

	private List<List<Object>> getDecimalOperandsAvg() {
		List<List<Object>> result = new ArrayList<List<Object>>();

		List<Object> list = new ArrayList<Object>();
		list.add(new BigDecimal(2));
		list.add(new BigDecimal(3));
		list.add(new BigDecimal(4));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new BigDecimal(2));
		list.add(new Integer(3));
		list.add(new BigDecimal(4));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new BigDecimal(2));
		list.add(new Long(3));
		list.add(new BigDecimal(4));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new BigDecimal(2));
		list.add(new Float(3));
		list.add(new BigDecimal(4));
		result.add(list);

		list = new ArrayList<Object>();
		list.add(new BigDecimal(2));
		list.add(new Double(3));
		list.add(new BigDecimal(4));
		result.add(list);

		return result;
	}
}
