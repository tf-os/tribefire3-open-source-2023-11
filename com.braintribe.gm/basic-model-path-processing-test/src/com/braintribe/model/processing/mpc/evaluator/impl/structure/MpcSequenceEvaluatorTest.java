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

import com.braintribe.model.bvd.predicate.InstanceOf;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.mpc.atomic.MpcTrue;
import com.braintribe.model.mpc.structure.MpcElementType;
import com.braintribe.model.mpc.structure.MpcSequence;
import com.braintribe.model.mpc.value.MpcElementAxis;
import com.braintribe.model.path.GmModelPathElementType;
import com.braintribe.model.processing.MpGenerator;
import com.braintribe.model.processing.misc.model.Person;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.AbstractMpcTest;

/**
 * Provides tests for {@link MpcSequenceEvaluator}.
 * 
 */
public class MpcSequenceEvaluatorTest extends AbstractMpcTest {

	/**
	 * Validate that a {@link MpcSequence} condition with CAPTURE where there are two sequences that will MATCH against
	 * a {@link IModelPathElement} which consists of "Root.name" ( {@link MpGenerator#getSimplePath()}).
	 */
	@Test
	public void testMatchingSequenceWithCapture() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();

		MpcElementType firstSequence = $.elementTypeCondition(GmModelPathElementType.Property);

		InstanceOf secondSequence = $.instanceOf(MpcElementAxis.value, Person.class.getName());

		MpcSequence condition = $.sequence(false, secondSequence, firstSequence);

		// run the matching method
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);

		// validate output
		assertThat(evaluationResult).isNotNull();
		// validate that the path has been consumed up to the root
		assertThat(evaluationResult.getPath()).isNull();
	}

	/**
	 * Validate that a {@link MpcSequence} condition with NO CAPTURE where there are two sequences that will MATCH
	 * against a {@link IModelPathElement} which consists of "Root.name" ( {@link MpGenerator#getSimplePath()}).
	 */
	@Test
	public void testMatchingSequenceWithNoCapture() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();

		MpcElementType firstSequence = $.elementTypeCondition(GmModelPathElementType.Property);

		InstanceOf secondSequence = $.instanceOf(MpcElementAxis.value, Person.class.getName());

		MpcSequence condition = $.sequence(true,  secondSequence, firstSequence);

		// run the matching method
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);

		// validate output
		assertThat(evaluationResult).isNotNull();
		// validate that the path has not been consumed
		assertThat(evaluationResult.getPath()).isNotNull();
		assertThat(evaluationResult.getPath().getDepth()).isEqualTo(1);
	}

	/**
	 * Validate that a {@link MpcSequence} condition where there are two sequences that will NOT MATCH
	 * against a {@link IModelPathElement} which consists of "Root.name" ( {@link MpGenerator#getSimplePath()}).
	 */
	@Test
	public void testNotMatchingSequence() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();

		MpcElementType firstSequence = $.elementTypeCondition(GmModelPathElementType.Property);

		InstanceOf secondSequence = $.instanceOf(MpcElementAxis.value, Person.class.getName());

		MpcSequence condition = $.sequence(false, firstSequence, secondSequence);

		// run the matching method
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);

		// validate that there was no match
		assertThat(evaluationResult).isNull();
	}
	
	
	/**
	 * Validate that a {@link MpcSequence} condition where there are two sequences that will NOT MATCH
	 * against a {@link IModelPathElement} which consists of "Root" ( {@link MpGenerator#getSimplePath()}).
	 */
	@Test
	public void testNotMatchingShortPathSequence() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();

		path = path.getPrevious();

		MpcTrue firstSequence = $.trueLiteral(false);

		MpcSequence condition = $.sequence(false, firstSequence, firstSequence);

		// run the matching method
		MpcMatch evaluationResult = MPC.mpcMatches(condition, path);

		// validate that there was no match
		assertThat(evaluationResult).isNull();
	}
}
