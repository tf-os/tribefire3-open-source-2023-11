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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.vde.evaluator.api.aspects.VariableProviderAspect;
import com.braintribe.model.processing.vde.evaluator.impl.root.VariableVde;
import com.braintribe.model.processing.vde.impl.misc.VariableCustomProvider;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link VariableVde}.
 *
 */
public class VariableVdeTest extends VdeTest {

	/**
	 * Validate that a {@link Variable} which has information that is available in a {@link VariableCustomProvider}, will evaluate properly.
	 */
	@Test
	public void testProviderWithValidData() throws Exception {

		// init object that will be tested
		VariableVde vde = new VariableVde();
		Variable vd = $.variable();
		vd.setName("key");
		VariableCustomProvider provider = new VariableCustomProvider();
		provider.add("key", "value");

		// validate input
		assertThat(provider).isNotNull();
		assertThat(vde).isNotNull();
		assertThat(vd).isNotNull();
		assertThat(vd.getName()).isEqualTo("key");

		// run the evaluate method
		Object result = evaluateWith(VariableProviderAspect.class, provider, vd);

		// validate output
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(String.class);
		assertThat(result).isEqualTo("value");
	}

	/**
	 * Validate that a {@link Variable} which has information that is NOT available in a {@link VariableCustomProvider}, will evaluate to null.
	 */
	@Test
	public void testProviderWithInValidData() throws Exception {

		// init object that will be tested
		VariableVde vde = new VariableVde();

		Variable vd = $.variable();
		vd.setName("otherKey");
		VariableCustomProvider provider = new VariableCustomProvider();
		provider.add("key", "value");

		// validate input
		assertThat(provider).isNotNull();
		assertThat(vde).isNotNull();
		assertThat(vd).isNotNull();
		assertThat(vd.getName()).isEqualTo("otherKey");

		// run the evaluate method
		Object result = evaluateWith(VariableProviderAspect.class, provider, vd);

		// validate output
		assertThat(result).isNull();
	}

}
