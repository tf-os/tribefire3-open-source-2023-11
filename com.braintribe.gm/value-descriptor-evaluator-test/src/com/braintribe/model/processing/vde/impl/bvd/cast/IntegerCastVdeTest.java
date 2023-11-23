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
package com.braintribe.model.processing.vde.impl.bvd.cast;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.bvd.cast.IntegerCast;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.cast.IntegerCastVde;
import com.braintribe.model.processing.vde.impl.VDGenerator;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link IntegerCastVde}.
 * 
 */
public class IntegerCastVdeTest extends VdeTest {

	public static VDGenerator $ = new VDGenerator(); 
	
	@Test
	public void testNumberToFloatCast() throws Exception {
		
		Object [] numbers = CastUtil.getAllPossibleNumberTypesArray();
		IntegerCast cast = $.integerCast();
		for(Object number: numbers){
			
			cast.setOperand(number);
			Object result = evaluate(cast);

			assertThat(result).isNotNull();
			assertThat(result).isInstanceOf(Integer.class);
		}		
	}

	@Test (expected=VdeRuntimeException.class)
	public void testBooleanToIntegerCastFail() throws Exception {
		Boolean x = new Boolean(true);

		IntegerCast cast = $.integerCast();
		cast.setOperand(x);

		evaluate(cast);
	}
}
