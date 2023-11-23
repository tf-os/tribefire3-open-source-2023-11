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
package com.braintribe.model.processing.vde.impl.root;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.root.EnumReferenceVde;
import com.braintribe.model.processing.vde.impl.misc.SalaryRange;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link EnumReferenceVde}.
 *
 */
public class EnumReferenceVdeTest extends VdeTest {

	@Test
	public void testEnumReference() throws Exception {
		EnumReference enumRef = $.enumReference();
		enumRef.setTypeSignature(SalaryRange.class.getName());
		enumRef.setConstant("medium");

		Object result = evaluate(enumRef);

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Enum.class);
		assertThat(result).isInstanceOf(SalaryRange.class);
		assertThat(result).isEqualTo(SalaryRange.medium);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testEnumReferenceFail() throws Exception {
		EnumReference enumRef = $.enumReference();
		enumRef.setTypeSignature(SalaryRange.class.getName());
		enumRef.setConstant("xyz");

		evaluate(enumRef);
	}

}
