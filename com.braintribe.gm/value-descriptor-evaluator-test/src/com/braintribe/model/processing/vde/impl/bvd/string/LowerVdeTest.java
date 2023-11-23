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

import org.junit.Test;

import com.braintribe.model.bvd.string.Lower;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;

public class LowerVdeTest extends AbstractStringVdeTest {
	
	@Test
	public void testStringLower() throws Exception {

		Lower stringFunction = $.lower();
		stringFunction.setOperand("HeLLo");

		Object result = evaluate(stringFunction);
		validateResult(result, "hello");

	}
	

	@Test (expected= VdeRuntimeException.class)
	public void testNullOperandsLower() throws Exception {

		Lower stringFunction = $.lower();
		
		evaluate(stringFunction);

	}
}
