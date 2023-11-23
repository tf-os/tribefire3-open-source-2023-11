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

import java.util.Date;

import org.junit.Test;

import com.braintribe.model.bvd.string.SubString;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;

public class SubStringVdeTest extends AbstractStringVdeTest {

	@Test
	public void testStringTwoindicesSubString() throws Exception {

		SubString stringFunction = $.substring();
		stringFunction.setOperand("HeLLo");
		stringFunction.setStartIndex(1);
		stringFunction.setEndIndex(3);

		Object result = evaluate(stringFunction);
		validateResult(result, "eL");

	}

	@Test
	public void testStringStartIndexSubString() throws Exception {

		SubString stringFunction = $.substring();
		stringFunction.setOperand("HeLLo");
		stringFunction.setStartIndex(1);

		Object result = evaluate(stringFunction);
		validateResult(result, "eLLo");

	}

	@Test(expected = VdeRuntimeException.class)
	public void testNullStartIndexSubString() throws Exception {

		SubString stringFunction = $.substring();

		evaluate(stringFunction);

	}

	@Test(expected = VdeRuntimeException.class)
	public void testRandomOperandSubString() throws Exception {

		SubString stringFunction = $.substring();
		stringFunction.setOperand(new Date());
		stringFunction.setStartIndex(1);

		evaluate(stringFunction);

	}
}
