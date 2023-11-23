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
package com.braintribe.utils;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.splitToLists;
import static com.braintribe.utils.lcd.CollectionTools2.splitToSets;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.Test;

public class CollectionTools2Tests {

	@Test
	public void split() {
		List<Integer> original = asList(0, 1, 2, 0, 1, 2, 0, 1, 2);

		List<Integer> expectedListPart = asList(0, 1, 2);
		Set<Integer> expectedSetPart = asSet(0, 1, 2);

		List<List<Integer>> lists = splitToLists(original, 3);
		List<Set<Integer>> sets = splitToSets(original, 3);

		for (List<Integer> listPart : lists) {
			assertThat(listPart).isEqualTo(expectedListPart);
		}

		for (Set<Integer> setPart : sets) {
			assertThat(setPart).isEqualTo(expectedSetPart);
		}
	}

}
