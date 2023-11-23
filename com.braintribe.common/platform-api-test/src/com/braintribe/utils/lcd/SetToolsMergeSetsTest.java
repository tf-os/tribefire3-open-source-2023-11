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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SetToolsMergeSetsTest {

	private Collection<Set<DummyElement>> allSets = null;

	@Before
	public void setUp() {
		this.allSets = new ArrayList<>();
	}

	@Test
	public void testMergeEmptySets() {
		SetTools.mergeSetsIfPossible(this.allSets, 0, true);
		assertThat(this.allSets).as("Should be empty").isEmpty();

		this.allSets.add(createDummySet(0, "EmptySet1"));
		assertThat(this.allSets).as("Has not correct size").hasSize(1);
		SetTools.mergeSetsIfPossible(this.allSets, 2, true);
		assertThat(this.allSets).as("Has not correct size").hasSize(1);

		this.allSets.add(createDummySet(0, "EmptySet2"));
		this.allSets.add(createDummySet(0, "EmptySet3"));
		this.allSets.add(createDummySet(0, "EmptySet4"));
		assertThat(this.allSets).as("Has not correct size").hasSize(4);
		SetTools.mergeSetsIfPossible(this.allSets, 2, true);
		assertThat(this.allSets).as("Has not correct size").hasSize(1);
	}

	@Test
	public void testMergeEmptySetWithOneMergableSet() {
		this.allSets.add(createDummySet(0, "EmptySet1"));
		this.allSets.add(createDummySet(1, "One1"));
		assertThat(this.allSets).as("Has not correct size").hasSize(2);
		SetTools.mergeSetsIfPossible(this.allSets, 2, true);
		assertThat(this.allSets).as("Has not correct size").hasSize(1);
	}

	@Test
	public void testMergeNull() {
		SetTools.mergeSetsIfPossible(null, 2, true);
	}

	@Test
	public void testMergeEmptySetWithOneNotMergableSet() {

		this.allSets.add(createDummySet(0, "EmptySet1"));
		this.allSets.add(createDummySet(3, "Three1"));
		assertThat(this.allSets).as("Has not correct size").hasSize(2);
		SetTools.mergeSetsIfPossible(this.allSets, 2, true);
		assertThat(this.allSets).as("Has not correct size").hasSize(2);
	}

	@Test
	public void testMergeIdenticalSets() {
		final Set<DummyElement> set1 = createDummySet(1, "One1");
		this.allSets.add(set1);
		this.allSets.add(set1);
		assertThat(this.allSets).as("Has not correct size").hasSize(2);
		SetTools.mergeSetsIfPossible(this.allSets, 2, true);
		assertThat(this.allSets).as("Has not correct size").hasSize(1);
	}

	@Test
	public void testMergeIdenticalSetsWithHashSet() {
		this.allSets = new HashSet<>();
		final Set<DummyElement> set1 = createDummySet(1, "One1");
		this.allSets.add(createDummySet(0, "EmptySet1"));
		this.allSets.add(set1);
		this.allSets.add(set1);
		assertThat(this.allSets).as("Has not correct size").hasSize(2);
		SetTools.mergeSetsIfPossible(this.allSets, 2, true);
		assertThat(this.allSets).as("Has not correct size").hasSize(1);
	}

	@Test
	public void testMergeSetsUnionSubsetsWithoutDuplicateCheck() {
		this.allSets = new HashSet<>();
		final Set<DummyElement> set1 = createDummySet(1, "One1");
		final Set<DummyElement> set2 = createDummySet(1, "One2");
		final Set<DummyElement> setUnion12 = new HashSet<>();
		setUnion12.addAll(set1);
		setUnion12.addAll(set2);
		this.allSets.add(set1);
		this.allSets.add(set2);
		this.allSets.add(setUnion12);
		assertThat(this.allSets).as("Has not correct size").hasSize(3);
		SetTools.mergeSetsIfPossible(this.allSets, 2, false);
		assertThat(this.allSets).as("Has not correct size").hasSize(1);
	}

	@Test
	public void testMergeSetsUnionSubsetsWithDuplicateCheck() {
		this.allSets = new HashSet<>();
		final Set<DummyElement> set1 = createDummySet(1, "One1");
		final Set<DummyElement> set2 = createDummySet(1, "One2");
		final Set<DummyElement> setUnion12 = new HashSet<>();
		setUnion12.addAll(set1);
		setUnion12.addAll(set2);
		this.allSets.add(set1);
		this.allSets.add(set2);
		this.allSets.add(setUnion12);
		assertThat(this.allSets).as("Has not correct size").hasSize(3);
		SetTools.mergeSetsIfPossible(this.allSets, 2, true);
		assertThat(this.allSets).as("Has not correct size").hasSize(1);
	}

	@Test
	public void testMergeSetsNegativeIdealSize() {
		this.allSets.add(createDummySet(1, "One1"));
		this.allSets.add(createDummySet(1, "One2"));
		this.allSets.add(createDummySet(2, "Two1"));
		this.allSets.add(createDummySet(2, "Two2"));
		this.allSets.add(createDummySet(2, "Two3"));
		assertThat(this.allSets).as("Has not correct size").hasSize(5);
		SetTools.mergeSetsIfPossible(this.allSets, -2, true);
		assertThat(this.allSets).as("Has not correct size").hasSize(5);
	}

	@Test
	public void testMergeSetsComplexCase() {
		this.allSets.add(createDummySet(0, "EmptySet1"));
		this.allSets.add(createDummySet(0, "EmptySet2"));
		this.allSets.add(createDummySet(1, "One1"));
		this.allSets.add(createDummySet(1, "One2"));
		this.allSets.add(createDummySet(1, "One3"));
		this.allSets.add(createDummySet(1, "One4"));
		this.allSets.add(createDummySet(1, "One5"));
		this.allSets.add(createDummySet(1, "One6"));
		this.allSets.add(createDummySet(2, "Two1"));
		this.allSets.add(createDummySet(2, "Two2"));
		this.allSets.add(createDummySet(2, "Two3"));
		this.allSets.add(createDummySet(2, "Two4"));
		this.allSets.add(createDummySet(2, "Two5"));
		this.allSets.add(createDummySet(2, "Two6"));
		this.allSets.add(createDummySet(3, "Three1"));
		this.allSets.add(createDummySet(3, "Three2"));
		this.allSets.add(createDummySet(5, "Five1"));
		this.allSets.add(createDummySet(5, "Five2"));
		assertThat(this.allSets).as("Has not correct size").hasSize(18);
		SetTools.mergeSetsIfPossible(this.allSets, 6, true);
		assertThat(this.allSets).as("Has not correct size").hasSize(6);
	}

	@Test
	public void testMergeSetsWithUnionsComplexCase() {
		this.allSets.add(createDummySet(0, "EmptySet1"));
		this.allSets.add(createDummySet(0, "EmptySet2"));
		final Set<DummyElement> set1 = createDummySet(1, "One1");
		final Set<DummyElement> set2 = createDummySet(1, "One2");
		final Set<DummyElement> set3 = createDummySet(1, "One3");
		final Set<DummyElement> set12 = new HashSet<>();
		final Set<DummyElement> set23 = new HashSet<>();
		set12.addAll(set1);
		set12.addAll(set2);
		set23.addAll(set2);
		set23.addAll(set3);
		this.allSets.add(set1);
		this.allSets.add(set12);
		this.allSets.add(set23);
		assertThat(this.allSets).as("Has not correct size").hasSize(5);
		SetTools.mergeSetsIfPossible(this.allSets, 2, true);
		assertThat(this.allSets).as("Has not correct size").hasSize(2);
	}

	@Test
	public void testMergeSetsIdealSizeReached() {
		this.allSets.add(createDummySet(2, "Two1"));
		this.allSets.add(createDummySet(2, "Two2"));
		this.allSets.add(createDummySet(3, "Three1"));
		this.allSets.add(createDummySet(3, "Three2"));
		assertThat(this.allSets).as("Has not correct size").hasSize(4);
		SetTools.mergeSetsIfPossible(this.allSets, 2, true);
		assertThat(this.allSets).as("Has not correct size").hasSize(4);
	}

	/* Helper method that prints the Collection of Sets */
	protected void printCollectionOfSets() {
		System.out.println("--------------------------------------------------------------------------");
		for (final Set<DummyElement> set : this.allSets) {
			System.out.print("Size = " + set.size());
			System.out.println("\t: " + set);
		}
	}

	class DummyElement {
		private final String parent;

		DummyElement(final String parent) {
			this.parent = parent;
		}

		@Override
		public String toString() {
			return "[" + this.parent + "]";
		}
	}

	private Set<DummyElement> createDummySet(final int size, final String setName) {
		final Set<DummyElement> set = new HashSet<>();

		for (int i = 0; i < size; i++) {
			final DummyElement dummyElement = new DummyElement(setName);
			set.add(dummyElement);
		}

		return set;
	}
}
