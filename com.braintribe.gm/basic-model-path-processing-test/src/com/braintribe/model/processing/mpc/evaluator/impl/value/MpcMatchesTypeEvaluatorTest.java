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
package com.braintribe.model.processing.mpc.evaluator.impl.value;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.generic.typecondition.basic.IsType;
import com.braintribe.model.mpc.value.MpcElementAxis;
import com.braintribe.model.mpc.value.MpcMatchesType;
import com.braintribe.model.processing.MpGenerator;
import com.braintribe.model.processing.misc.model.Person;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.AbstractMpcTest;

/**
 * Provides tests for {@link MpcMatchesTypeEvaluator}.
 * 
 */
public class MpcMatchesTypeEvaluatorTest extends AbstractMpcTest {

	/**
	 * Validate that a {@link MpcMatchesType} condition which is preset with a {@link IsAssignableTo} to
	 * {@link GenericEntity} will MATCH against a {@link IModelPathElement} which consists of only a "Root" Element of
	 * type {@link Person}.
	 */
	@Test
	public void testMatchWithEntityTypeStrategyAssignable() throws Exception {
		MpcMatchesType condition = $.matchesType(MpcElementAxis.value, TypeConditions.isAssignableTo(GenericEntity.T));

		// path points to root only
		IModelPathElement path = MpGenerator.getSimplePath().getPrevious();

		// run the matching method
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);

		// validate the output
		assertThat(evaluationResult).isNotNull();
		assertThat(evaluationResult.getPath()).isNull();
	}

	/**
	 * Validate that a {@link MpcMatchesType} condition which is preset with a {@link IsType} for {@link GenericEntity}
	 * will NOT MATCH against a {@link IModelPathElement} which consists of only a "Root" Element of type
	 * {@link Person}.
	 */
	@Test
	public void testNoMatchEntityTypeStrategyEquals() throws Exception {
		MpcMatchesType condition = $.matchesType(MpcElementAxis.value, TypeConditions.isType(GenericEntity.T));

		// path points to root only
		IModelPathElement path = MpGenerator.getSimplePath().getPrevious();

		// run the matching method
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);

		// validate the output
		assertThat(evaluationResult).isNull();
	}

}
