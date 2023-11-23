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

import org.junit.Test;

import com.braintribe.model.bvd.predicate.InstanceOf;
import com.braintribe.model.processing.vde.impl.misc.Child;
import com.braintribe.model.processing.vde.impl.misc.Person;

public class InstanceOfVdeTest extends AbstractPredicateVdeTest {

	
	@Test
	public void testStringStrinInstanceOf() throws Exception {

		InstanceOf predicate = $.instanceOf();
		Person value1 = Person.T.create();
		String value2 = Person.class.getName();

		predicate.setLeftOperand(value1);
		predicate.setRightOperand(value2);

		Object result = evaluate(predicate);
		validatePositiveResult(result);

	}
	
	@Test
	public void testStringIntegerInstanceOf() throws Exception {

		InstanceOf predicate = $.instanceOf();
		String value1 = "hello";
		Integer value2 = new Integer(1);

		predicate.setLeftOperand(value1);
		predicate.setRightOperand(value2);

		Object result = evaluate(predicate);
		validateNegativeResult(result);

	}
	
	@Test
	public void testChildPersonInstanceOf() throws Exception {

		InstanceOf predicate = $.instanceOf();
		Child value1 = Child.T.create();
		Person value2 = Person.T.create();

		predicate.setLeftOperand(value1);
		predicate.setRightOperand(value2);

		Object result = evaluate(predicate);
		validateNegativeResult(result);

	}

	@Test
	public void testChildChildInstanceOf() throws Exception {

		InstanceOf predicate = $.instanceOf();
		Child value1 = Child.T.create();
		Child value2 = Child.T.create();

		predicate.setLeftOperand(value1);
		predicate.setRightOperand(value2);

		Object result = evaluate(predicate);
		validatePositiveResult(result);

	}
	
}
