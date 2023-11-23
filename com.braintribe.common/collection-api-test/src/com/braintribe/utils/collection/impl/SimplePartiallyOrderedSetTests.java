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
package com.braintribe.utils.collection.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Test for {@link SimplePartiallyOrderedSet}
 */
public class SimplePartiallyOrderedSetTests {

	private final SimplePartiallyOrderedSet<String, Integer> poSet = new SimplePartiallyOrderedSet<>();

	@Test
	public void emptyHasCorrectState() {
		assertThat(poSet.isEmpty()).isTrue();
		assertThat(poSet.stream()).isEmpty();
	}

	@Test
	public void noAdd_StillEmpty() {
		poSet.with(1);

		assertThat(poSet.isEmpty()).isTrue();
		assertThat(poSet.stream()).isEmpty();
	}

	@Test(expected = Exception.class)
	public void detectsSimpleInconsistency() {
		poSet.after(1).after(2).after(3).before(2);
	}

    @Test
    public void worksWithNoIdentifiers() {
        poSet.add("1");
        poSet.add("2");
        poSet.add("3");

        assertThat(poSet.stream()).containsExactly("1", "2", "3");
    }	

	@Test
	public void addSingleElement() {
		poSet.with(1).add("1");

		assertThat(poSet.stream()).containsExactly("1");
	}

	@Test
	public void addNull() {
		poSet.with(1).add(null);

		assertThat(poSet.stream()).containsExactly((String) null);
	}

	@Test
	public void addElementAgainFails() {
		poSet.with(1).add("1");

		try {
			poSet.with(1);
			fail("Exception should have been thrown as there is already a value for given identifier.");
		} catch (final Exception e) {
			// expected
		}
	}

	@Test
	public void addElementBeforePreviousAddFinishedFails() {
		poSet.with(1);

		try {
			poSet.with(1);
			fail("Exception should have been thrown as we are already in the process of adding.");
		} catch (final Exception e) {
			// expected
		}
	}

	@Test
	public void preservesCorrectOrder_Simple() {
		poSet.with(2).add("2");
		poSet.with(4).after(2).add("4");
		poSet.with(1).before(2).add("1");
		poSet.with(3).after(2).before(4).add("3");

		assertThat(poSet.stream()).containsExactly("1", "2", "3", "4");
	}

	@Test
	public void preservesCorrectOrder_MoreComplex() {
		poSet.with(3).after(1).after(2).before(4).add("3");
		poSet.with(2).before(4).after(1).add("2");
		poSet.with(1).add("1");
		poSet.with(4).after(1).add("4");

		assertThat(poSet.stream()).containsExactly("1", "2", "3", "4");
	}

	@Test
	public void preservesCorrectOrder_MoreComplex2() {
		poSet.with(3).add("3");
		poSet.with(4).after(3).add("4");
		poSet.with(1).add("1");
		poSet.with(2).after(1).before(3).add("2");

		assertThat(poSet.stream()).containsExactly("1", "2", "3", "4");
	}

	@Test
	public void preservesCorrectOrder_MoreComplex3() {
		poSet.with(1).add("1");
		poSet.with(2).after(1).add("2");
		poSet.with(4).add("4");
		poSet.with(3).after(2).before(4).add("3");

		assertThat(poSet.stream()).containsExactly("1", "2", "3", "4");
	}

	@Test
	public void detectsTransitiveInconsistencies() {
		poSet.with(2).after(1).add("2"); // 1->2
		poSet.with(3).after(2).add("3"); // 2->3
		try {
			poSet.with(1).after(3).add("1"); // 3->1 (error)
			fail("Exception should have been thrown due to inconsistent ordering.");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage()).contains("3->1->2->3");
		}
	}

	@Test
	public void stillWorksAfterInconsistencyDetected() {
		detectsTransitiveInconsistencies();

		poSet.with(1).before(3).add("1");

		assertThat(poSet.stream()).containsExactly("1", "2", "3");
	}

	@Test
	public void containsPositionWorks() {
		poSet.with(1).before(2).add("1");

		assertThat(poSet.containsPosition(1)).isTrue();
		assertThat(poSet.containsPosition(2)).isTrue();
		assertThat(poSet.containsPosition(3)).isFalse();
	}

	@Test
	public void containsElementOnPositionWorks() {
		poSet.with(1).before(3).add("1");
		poSet.with(2).add(null);

		assertThat(poSet.containsPosition(1)).isTrue();
		assertThat(poSet.containsPosition(2)).isTrue();
		assertThat(poSet.containsPosition(3)).isTrue();

		assertThat(poSet.containsElementOnPosition(1)).isTrue();
		assertThat(poSet.containsElementOnPosition(2)).isTrue();
		assertThat(poSet.containsElementOnPosition(3)).isFalse();
	}

	@Test
	public void replaceWorks() {
		prepareStandard123();

		assertThat(poSet.replace(2, "22")).isEqualTo("2");
		assertThat(poSet.stream()).containsExactly("1", "22", "3");
	}

	@Test
	public void removeWorks() {
		prepareStandard123();

		assertThat(poSet.remove("2")).isTrue();
		assertThat(poSet.stream()).containsExactly("1", "3");

		assertThat(poSet.remove("5")).isFalse();
		assertThat(poSet.stream()).containsExactly("1", "3");
	}

	@Test
	public void removeElement() {
		prepareStandard123();

		assertThat(poSet.removeElement(2)).isEqualTo("2");
		assertThat(poSet.stream()).containsExactly("1", "3");
	}

	private void prepareStandard123() {
		poSet.with(1).add("1");
		poSet.with(2).after(1).add("2");
		poSet.with(3).after(2).add("3");

		assertThat(poSet.stream()).containsExactly("1", "2", "3");
	}

}
