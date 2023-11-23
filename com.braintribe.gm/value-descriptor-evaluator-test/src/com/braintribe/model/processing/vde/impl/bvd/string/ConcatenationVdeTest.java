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
package com.braintribe.model.processing.vde.impl.bvd.string;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.braintribe.model.bvd.string.Concatenation;
import com.braintribe.model.bvd.string.Lower;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;

public class ConcatenationVdeTest extends AbstractStringVdeTest {

	@Test
	public void testMultipleStringConcatenation() throws Exception {

		Concatenation stringFunction = $.concatenation();
		List<Object> list = new ArrayList<Object>();
		list.add("hello");
		list.add("1");
		list.add("2");
		list.add("3");
		list.add("4");
		stringFunction.setOperands(list);

		Object result = evaluate(stringFunction);
		validateResult(result, "hello1234");

	}

	@Test
	public void testSingleStringConcatenation() throws Exception {

		Concatenation stringFunction = $.concatenation();
		List<Object> list = new ArrayList<Object>();
		list.add("hello");
		stringFunction.setOperands(list);

		Object result = evaluate(stringFunction);
		validateResult(result, "hello");

	}

	@Test
	public void testNullOperandsConcatenation() throws Exception {

		Concatenation stringFunction = $.concatenation();

		Object result = evaluate(stringFunction);
		validateResult(result, "");

	}

	@Test(expected = VdeRuntimeException.class)
	public void testWrongOperandsConcatenationFail() throws Exception {

		Concatenation stringFunction = $.concatenation();
		List<Object> list = new ArrayList<Object>();
		list.add("hello");
		list.add(new Date()); // not string
		stringFunction.setOperands(list);

		evaluate(stringFunction);

	}
	
	@Test
	public void testMultipleTypeStringConcatenation() throws Exception {

		Concatenation stringFunction = $.concatenation();
		List<Object> list = new ArrayList<Object>();
		Lower lower = Lower.T.create();
		lower.setOperand("HeLLo");
		list.add(lower);
		list.add("1");
		list.add("2");
		list.add("3");
		list.add("4");
		stringFunction.setOperands(list);

		Object result = evaluate(stringFunction);
		validateResult(result, "hello1234");

	}
	
}
