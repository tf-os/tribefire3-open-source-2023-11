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
import com.braintribe.model.mpc.atomic.MpcTrue;
import com.braintribe.model.processing.MpGenerator;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.AbstractMpcTest;

/**
 * Provides tests for {@link MpcTrueEvaluator}.
 * 
 */
public class MpcTrueEvaluatorTest extends AbstractMpcTest {

	/**
	 * Validate that a {@link MpcTrue} condition will MATCH against anything and consume one element
	 */
	@Test
	public void testTrueCaptureMatch() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();
		MpcTrue condition = $.trueLiteral(false);

		// run the matching method
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);
		
		// validate path has been consumed
		assertThat(evaluationResult).isNotNull();
		assertThat(evaluationResult.getPath().getDepth()).isEqualTo(0);

	}
	
	/**
	 * Validate that a {@link MpcTrue} condition will MATCH against anything and consume nothing
	 */
	@Test
	public void testTrueNoCaptureMatch() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();
		MpcTrue condition = $.trueLiteral(true);

		// run the matching method
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);
		
		// validate path has not been consumed
		assertThat(evaluationResult).isNotNull();
		assertThat(evaluationResult.getPath().getDepth()).isEqualTo(1);

	}
	
	/**
	 * Validate that a {@link MpcTrue} condition will MATCH against null path
	 */
	@Test
	public void testNullMatch() throws Exception {

		MpcTrue condition = $.trueLiteral(false);

		// run the matching method
		MpcMatch evaluationResult = MPC.mpcMatches(condition, null);
		
		// validate there is a match with path null
		assertThat(evaluationResult).isNotNull();
		assertThat(evaluationResult.getPath()).isNull();

	}

}
