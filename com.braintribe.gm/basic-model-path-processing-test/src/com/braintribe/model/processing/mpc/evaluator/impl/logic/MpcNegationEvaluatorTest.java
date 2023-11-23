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
package com.braintribe.model.processing.mpc.evaluator.impl.logic;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.mpc.logic.MpcNegation;
import com.braintribe.model.processing.MpGenerator;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.AbstractMpcTest;

/**
 * Provides tests for {@link MpcNegationEvaluator}.
 * 
 */
public class MpcNegationEvaluatorTest extends AbstractMpcTest {

	/**
	 * Validate that a {@link MpcNegation} condition of {@link $#negationCondition()} which will MATCH against a
	 * {@link IModelPathElement} which consists of "Root.name" ( {@link MpGenerator#getSimplePath()}).
	 */
	@Test
	public void testSimpleNegationMatch() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();

		MpcNegation condition = $.negationCondition();

		// evaluate the condition against the path
		MpcMatch evalResult = MPC.mpcMatches(condition, path);

		// validate output
		assertThat(evalResult).isNull();
	}

	/**
	 * Validate that a {@link MpcNegation} condition of {@link $#negationCondition()} which will NOT MATCH against a
	 * {@link IModelPathElement} which consists of "Root.favouriteColours[1]" ( {@link MpGenerator#getTernaryPath()}).
	 */
	@Test
	public void testSimpleNegationNoMatch() throws Exception {

		IModelPathElement path = MpGenerator.getTernaryPath();

		MpcNegation condition = $.negationCondition();

		// evaluate the condition against the path
		MpcMatch evalResult = MPC.mpcMatches(condition, path);

		assertThat(evalResult).isNotNull();
		assertThat(evalResult.getPath()).isNotNull();
		// check the path consists of root and another element
		assertThat(evalResult.getPath().getDepth()).isEqualTo(2);
		// validate that the current element is indeed a list item
		assertThat(evalResult.getPath().getElementType()).isEqualTo(ModelPathElementType.ListItem);
		// validate that the previous element is indeed property
		assertThat(evalResult.getPath().getPrevious().getElementType()).isEqualTo(ModelPathElementType.Property);
		// validate that the first element is indeed the root
		assertThat(evalResult.getPath().getPrevious().getPrevious().getElementType()).isEqualTo(ModelPathElementType.Root);

	}

}
