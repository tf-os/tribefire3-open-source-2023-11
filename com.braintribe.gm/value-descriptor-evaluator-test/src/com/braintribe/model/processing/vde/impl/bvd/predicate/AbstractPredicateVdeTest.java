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

import static org.assertj.core.api.Assertions.assertThat;

import com.braintribe.model.processing.vde.impl.VDGenerator;
import com.braintribe.model.processing.vde.test.VdeTest;

public abstract class AbstractPredicateVdeTest extends VdeTest {

	public static VDGenerator $ = new VDGenerator(); 
	
	private void validateResult(Object result, boolean expectedValue){
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(new Boolean(expectedValue));
	}

	protected void validatePositiveResult(Object result){
		validateResult(result, true);
	}
	
	protected void validateNegativeResult(Object result){
		validateResult(result, false);
	}
	
}
