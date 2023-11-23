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
package com.braintribe.model.processing.mpc.evaluator.impl.atomic;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.mpc.atomic.MpcFalse;
import com.braintribe.model.mpc.logic.MpcNegation;
import com.braintribe.model.processing.MpGenerator;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.AbstractMpcTest;

/**
 * Provides tests for {@link MpcFalseEvaluator}.
 * 
 */
public class MpcFalseEvaluatorTest extends AbstractMpcTest {

	/**
	 * Validate that a {@link MpcFalse} condition will NOT MATCH against anything 
	 */
	@Test
	public void testFalseMatch() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();
		MpcFalse condition = $.falseLiteral();

		// run the matching method
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);
		
		// validate null result
		assertThat(evaluationResult).isNull();

	}
	
	/**
	 * Validate that a negation of {@link MpcFalse} condition will MATCH against anything and consume one element
	 */
	@Test
	public void testNegationFalseMatch() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();

		MpcNegation condition = $.negation($.falseLiteral());
		
		// run the matching method
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);
		
		// validate that same path has been returned
		assertThat(evaluationResult).isNotNull();
		assertThat(evaluationResult.getPath().getDepth()).isEqualTo(1);

	}

}
