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
package com.braintribe.model.processing.mpc.evaluator.impl.structure;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.mpc.structure.MpcPropertyName;
import com.braintribe.model.processing.MpGenerator;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.AbstractMpcTest;

/**
 * Provides tests for {@link MpcPropertyNameEvaluator}.
 * 
 */
public class MpcPropertyNameEvaluatorTest extends AbstractMpcTest {

	/**
	 * Validate that a {@link MpcPropertyName} condition where it is set to "name" will MATCH against a
	 * {@link IModelPathElement} which consists of "Root.name" ( {@link MpGenerator#getSimplePath()}).
	 */
	@Test
	public void testMatchingPropertyName() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();

		MpcPropertyName condition = $.propertyNameCondition("name");

		// evaluate the condition against the path
		MpcMatch evalResult = MPC.mpcMatches(condition, path);

		// validate output
		assertThat(evalResult).isNotNull();
		// validate that the result is one item shorter
		assertThat(evalResult.getPath().getDepth()).isEqualTo(0);
		// validate that the current element is indeed the root
		assertThat(evalResult.getPath().getElementType()).isEqualTo(ModelPathElementType.Root);
	}

	/**
	 * Validate that a {@link MpcPropertyName} condition where it is set to "notName" will NOT MATCH against a
	 * {@link IModelPathElement} which consists of "Root.name" ( {@link MpGenerator#getSimplePath()}).
	 */
	@Test
	public void testNotMatchingPropertyName() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();

		MpcPropertyName condition = $.propertyNameCondition("notName");

		// evaluate the condition against the path
		MpcMatch evalResult = MPC.mpcMatches(condition, path);

		// validate output
		assertThat(evalResult).isNull();

	}

}
