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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.braintribe.model.bvd.predicate.In;

public class InVdeTest extends AbstractPredicateVdeTest {

	
	@Test
	public void testStringIn() throws Exception {

		In predicate = $.in();
		String value = "hello";
		
		List<String> list = new ArrayList<String>();
		list.add(value);
		list.add("1");
		list.add("2");
		list.add("3");
		predicate.setLeftOperand(value);
		predicate.setRightOperand(list);

		Object result = evaluate(predicate);
		validatePositiveResult(result);

		predicate.setLeftOperand("H");

		result = evaluate(predicate);
		validateNegativeResult(result);

	}
	
	@Test
	public void testCollectionIn() throws Exception {

		In predicate = $.in();
		
		List<String> value1 = new ArrayList<String>();
		value1.add("hello");
		List<String> value2 = new ArrayList<String>();
		value1.add("hello2");
		List<String> value3 = new ArrayList<String>();
		value1.add("hello3");
		
		List<Object> list = new ArrayList<Object>();
		list.add(value1);
		list.add(value2);
		list.add(value3);
		
		predicate.setLeftOperand(value1);
		predicate.setRightOperand(list);

		Object result = evaluate(predicate);
		validatePositiveResult(result);

		predicate.setLeftOperand("H");

		result = evaluate(predicate);
		validateNegativeResult(result);

	}
}
