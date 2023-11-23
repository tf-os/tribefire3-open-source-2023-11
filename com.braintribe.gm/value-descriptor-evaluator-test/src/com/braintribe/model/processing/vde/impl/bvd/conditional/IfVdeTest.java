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
package com.braintribe.model.processing.vde.impl.bvd.conditional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.bvd.conditional.If;
import com.braintribe.model.bvd.predicate.Equal;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToStringVde;
import com.braintribe.model.processing.vde.impl.VDGenerator;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link ToStringVde}.
 * 
 */
public class IfVdeTest extends VdeTest {

	@Test
	public void testIfStringEqualThen() throws Exception {
		String left = "foo";
		String right = "foo";
		
		Equal equal = VDGenerator.$.equal(left, right);
		If _if = VDGenerator.$._if(equal, true, false);
		

		Object result = evaluate(_if);
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void testIfStringEqualElse() throws Exception {
		String left = "foo";
		String right = "foo2";
		
		Equal equal = VDGenerator.$.equal(left, right);
		If _if = VDGenerator.$._if(equal, true, false);
		

		Object result = evaluate(_if);
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(false);
	}


}
