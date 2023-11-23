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
package com.braintribe.model.processing.manipulation.basic.normalization;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fest.assertions.Assertions;

import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode;

/**
 * 
 */
public abstract class AbstractSimpleNormalizerTests extends AbstractNormalizerTests {

	@Override
	protected void normalize() {
		List<AtomicManipulation> manipulations = newList(recordedManipulations);
		NormalizationContext context = new NormalizationContext(trackingMode == ManipulationTrackingMode.GLOBAL);
		normalizedManipulations = new SimpleManipulationNormalizer(manipulations, context).normalize();
	}

	// ####################################
	// ## . . . . . Assertions . . . . . ##
	// ####################################

	protected void assertEmpty() {
		Assertions.assertThat(recordedManipulations).isNotNull().isNotEmpty();
		Assertions.assertThat(normalizedManipulations).isNotNull().isEmpty();
	}

	/**
	 * The parameters are the expected positions of the recorded manipulations in the "normaliedManipulations" list. If some manipulation is expected
	 * to be removed during normalization, one indicates this with a negative value. For example, if from the 3 recorded manipulations we expect the
	 * one in the middle to be removed, one would verify that by calling this method with parameters: <code>(0, -1, 1)</code>.
	 */
	protected void assertPositions(int... indices) {
		if (indices.length != recordedManipulations.size()) {
			throw new RuntimeException("WRONG ARRAY OF INDICES. RECORDED MANIPULATIONS: " + recordedManipulations.size()
					+ ", BUT SPECIFIED POSITIONS: " + indices.length);
		}

		Set<AtomicManipulation> normalizedSet = new HashSet<AtomicManipulation>(normalizedManipulations);

		for (int i = 0; i < indices.length; i++) {
			int expectedPosition = indices[i];
			AtomicManipulation recorded = recordedManipulations.get(i);

			if (expectedPosition < 0) {
				Assertions.assertThat(normalizedSet).excludes(recorded);
			} else {
				Assertions.assertThat(normalizedManipulations.get(expectedPosition)).as("Wrong manipulation for index: " + i).isSameAs(recorded);
			}
		}
	}
}
