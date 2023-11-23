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
package com.braintribe.utils.lcd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @deprecated see {@link PartiallyOrderedList}
 */
@Deprecated
public class PartiallyOrderedListTests {

	@Test(expected = Exception.class)
	public void detectsSimpleInconsistency() {
		new PartiallyOrderedList<String, Integer>().after(1).after(2).after(3).before(2);
	}

	@Test
	public void detectsTransitiveInconsistencies() {
		final PartiallyOrderedList<String, Integer> list = new PartiallyOrderedList<>();
		list.with(2).after(1).add("2");
		list.with(2).after(1).add("2");
		list.with(3).after(2).add("3");
		try {
			list.with(1).after(3).add("1"); // must throw Exception
			fail("Exception should have been thrown due to inconsistent ordering.");
		} catch (final Exception e) {
			// expected
		}
	}

	@Test
	public void preservesCorrectOrder_Simple() {
		final PartiallyOrderedList<String, Integer> reg = new PartiallyOrderedList<>();

		reg.with(2).add("2");
		reg.with(4).after(2).add("4");
		reg.with(1).before(2).add("1");
		reg.with(3).after(2).before(4).add("3");

		assertThat(reg.list()).hasSize(4).containsSequence("1", "2", "3", "4");
	}

	@Test
	public void preservesCorrectOrder_MoreComplex() {
		final PartiallyOrderedList<String, Integer> reg = new PartiallyOrderedList<>();

		reg.with(3).after(1).after(2).before(4).add("3");
		reg.with(2).before(4).add("2");
		reg.with(1).add("1");
		reg.with(4).after(1).add("4");

		assertThat(reg.list()).hasSize(4).containsSequence("1", "2", "3", "4");
	}

	@Test
	public void worksWithNoIdentifiers() {
		final PartiallyOrderedList<String, Integer> reg = new PartiallyOrderedList<>();
		reg.add("1");
		reg.add("2");
		reg.add("3");

		assertThat(reg.list()).hasSize(3);
	}

	@Test
	public void removeWorks() {
		final PartiallyOrderedList<String, Integer> reg = new PartiallyOrderedList<>();

		reg.with(1).add("1");
		reg.with(2).after(1).add("2");
		reg.with(3).after(2).add("3");

		assertThat(reg.list()).hasSize(3).containsSequence("1", "2", "3");

		reg.remove("2");

		assertThat(reg.list()).hasSize(2).containsSequence("1", "3");
	}

}
