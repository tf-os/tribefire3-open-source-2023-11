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
import com.braintribe.model.mpc.structure.MpcElementType;
import com.braintribe.model.path.GmModelPathElementType;
import com.braintribe.model.processing.MpGenerator;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.AbstractMpcTest;

/**
 * Provides tests for {@link MpcElementTypeEvaluator}.
 * 
 */
public class MpcElementTypeEvaluatorTest extends AbstractMpcTest {

	/**
	 * Validate that a {@link MpcElementType} condition where elementType will loop through all
	 * {@link GmModelPathElementType} MATCH against a {@link IModelPathElement} which consists of "Root.name" (
	 * {@link MpGenerator#getSimplePath()}), according to the type of the operator (Property).
	 */
	@Test
	public void testPropertyElementTypeMatch() throws Exception {

		IModelPathElement path = MpGenerator.getSimplePath();

		int gmModelPathElementLength = GmModelPathElementType.values().length;

		for (int i = 0; i < gmModelPathElementLength; i++) {
			GmModelPathElementType currentGMType = getGmModelPathElementType(i);
			MpcElementType condition = $.elementTypeCondition(currentGMType);

			// Entry point is not supported
			if (condition.getElementType() == GmModelPathElementType.EntryPoint)
				continue;

			// evaluate the condition against the path
			MpcMatch evalResult = MPC.mpcMatches(condition, path);

			// validate output
			switch (condition.getElementType()) {
				case EntryPoint:
				case Root:
				case SetItem:
				case ListItem:
				case MapKey:
				case MapValue:
					assertThat(evalResult).isNull();
					break;
				case Property:
					assertThat(evalResult).isNotNull();
					// validate that the result is one item shorter
					assertThat(evalResult.getPath().getDepth()).isEqualTo(0);
					// validate that the current element is indeed the root
					assertThat(evalResult.getPath().getElementType()).isEqualTo(ModelPathElementType.Root);
					break;

				default:
					break;

			}
		}
	}
	
	/**
	 * Validate that a {@link MpcElementType} condition where elementType will loop through all
	 * {@link GmModelPathElementType} MATCH against a {@link IModelPathElement} which consists of "Root.favouriteColours[1]" (
	 * {@link MpGenerator#getTernaryPath()}), according to the type of the operator (ListItem).
	 */
	@Test
	public void testListElementTypeMatch() throws Exception {

		IModelPathElement path = MpGenerator.getTernaryPath();

		int gmModelPathElementLength = GmModelPathElementType.values().length;

		for (int i = 0; i < gmModelPathElementLength; i++) {
			GmModelPathElementType currentGMType = getGmModelPathElementType(i);
			MpcElementType condition = $.elementTypeCondition(currentGMType);

			// Entry point is not supported
			if (condition.getElementType() == GmModelPathElementType.EntryPoint)
				continue;

			// evaluate the condition against the path
			MpcMatch evalResult = MPC.mpcMatches(condition, path);

			// validate output
			switch (condition.getElementType()) {
				case EntryPoint:
				case Root:
				case SetItem:
				case Property:
				case MapKey:
				case MapValue:
					assertThat(evalResult).isNull();
					break;
				case ListItem:
					assertThat(evalResult).isNotNull();
					// validate that the result is one item shorter
					assertThat(evalResult.getPath().getDepth()).isEqualTo(1);
					// validate that the current element is indeed the root
					assertThat(evalResult.getPath().getElementType()).isEqualTo(ModelPathElementType.Property);
					break;

				default:
					break;

			}
		}
	}

	/**
	 * @param position
	 *            index in array
	 * @return The {@link GmModelPathElementType} at the indicated position
	 */
	private static GmModelPathElementType getGmModelPathElementType(int position) {
		return GmModelPathElementType.values()[position];
	}
}
