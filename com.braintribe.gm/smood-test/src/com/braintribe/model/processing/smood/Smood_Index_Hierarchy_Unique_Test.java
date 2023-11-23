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
package com.braintribe.model.processing.smood;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.test.model.indexed.IndexedA;
import com.braintribe.model.processing.query.test.model.indexed.IndexedAB;
import com.braintribe.model.processing.query.test.model.indexed.IndexedASub;
import com.braintribe.model.processing.query.test.model.indexed.IndexedB;
import com.braintribe.model.processing.query.test.model.indexed.IndexedBSub;
import com.braintribe.model.processing.query.test.model.indexed.IsIndexed;
import com.braintribe.model.processing.smood.population.SmoodIndexTools;
import com.braintribe.model.processing.smood.test.AbstractSmoodTests;

/**
 * 
 */
public class Smood_Index_Hierarchy_Unique_Test extends AbstractSmoodTests {

	// ###################################################
	// ## . . . . . . . Indices are there . . . . . . . ##
	// ###################################################

	@Test
	public void indicesAreThere() {
		registerAtSmood(indexedAB("uniqueAB", "ambigAB"));
		assertHasBothIndices(IndexedA.T);
		assertHasBothIndices(IndexedB.T);
		assertHasBothIndices(IndexedAB.T);

		registerAtSmood(indexedASub("uniqueAS", "ambigAS"));
		assertHasBothIndices(IndexedASub.T);

		registerAtSmood(indexedBSub("uniqueBS", "ambigBS"));
		assertHasBothIndices(IndexedBSub.T);
	}

	private void assertHasBothIndices(EntityType<?> et) {
		String ts = et.getTypeSignature();
		assertThat(smood.provideIndexInfo(ts, IndexedA.unique)).isNotNull();
		assertThat(smood.provideIndexInfo(ts, IndexedA.ambig)).isNotNull();
	}

	// ###################################################
	// ## . . . . . . . . Lookup works . . . . . . . . .##
	// ###################################################

	/** We test that all levels relevant for IndexedAB (i.e. super-types) are able to resolve the entity for given value. */
	@Test
	public void unique_worksOnAllLevels() {
		registerAtSmood(indexedAB("unique", "ambig"));

		findUniqueForLevel(IndexedAB.T);
		findUniqueForLevel(IndexedA.T);
		findUniqueForLevel(IndexedB.T);
	}

	private void findUniqueForLevel(EntityType<?> et) {
		GenericEntity entity = smood.getValueForIndex(SmoodIndexTools.indexId(et, IndexedA.unique), "unique");
		assertThat(entity).isNotNull();
	}

	/** We test that all levels relevant for IndexedAB (i.e. super-types) are able to resolve the entity for given value. */
	@Test
	public void nonUnique_worksOnAllLevels() {
		registerAtSmood(indexedAB("unique", "ambig"));

		findAmbigForLevel(IndexedAB.T);
		findAmbigForLevel(IndexedA.T);
		findAmbigForLevel(IndexedB.T);
	}

	private void findAmbigForLevel(EntityType<?> et) {
		GenericEntity entity = smood.getValueForIndex(SmoodIndexTools.indexId(et, IndexedA.ambig), "ambig");
		assertThat(entity).isNotNull();
	}

	// ###################################################
	// ## . . . . . . Uniqueness is guarded . . . . . . ##
	// ###################################################

	@Test(expected = IllegalStateException.class)
	public void uniqueViolation_SameType() {
		registerAtSmood(indexedB("unique", "one"));

		// this throws IllegalStateException: Another entity is already indexed
		registerAtSmood(indexedB("unique", "two"));
	}

	@Test(expected = IllegalStateException.class)
	public void uniqueViolation_SameType2() {
		registerAtSmood(indexedAB("unique", "one"));

		// this throws IllegalStateException: Another entity is already indexed
		registerAtSmood(indexedAB("unique", "two"));
	}

	@Test(expected = IllegalStateException.class)
	public void uniqueViolation_WithSubType() {
		registerAtSmood(indexedB("unique", "super"));

		// this throws IllegalStateException: Another entity is already indexed
		registerAtSmood(indexedAB("unique", "sub"));
	}

	@Test(expected = IllegalStateException.class)
	public void uniqueViolation_WithSuperType() {
		registerAtSmood(indexedAB("unique", "sub"));

		// this throws IllegalStateException: Another entity is already indexed
		registerAtSmood(indexedB("unique", "super"));
	}

	@Test(expected = IllegalStateException.class)
	public void uniqueViolation_WithTwoSubTypes() {
		registerAtSmood(indexedAB("unique", "sub"));

		// this throws IllegalStateException: Another entity is already indexed
		registerAtSmood(indexedBSub("unique", "super"));
	}

	@Test
	public void noUniqueViolation_WhenSuperIsNotUnique() {
		registerAtSmood(indexedAB("unique", "sub"));

		// The property called "unique" is not unique on IndexedA level, thus there is no level at which there would be a uniqueness violation
		registerAtSmood(indexedA("unique", "super"));
	}

	// ###################################################
	// ## . . . . . . . . . Helpers . . . . . . . . . . ##
	// ###################################################

	private IndexedA indexedA(String unique, String ambig) {
		return indexed(IndexedA.T, unique, ambig);
	}

	private IndexedASub indexedASub(String unique, String ambig) {
		return indexed(IndexedASub.T, unique, ambig);
	}

	private IndexedB indexedB(String unique, String ambig) {
		return indexed(IndexedB.T, unique, ambig);
	}

	private IndexedBSub indexedBSub(String unique, String ambig) {
		return indexed(IndexedBSub.T, unique, ambig);
	}

	private IndexedAB indexedAB(String unique, String ambig) {
		return indexed(IndexedAB.T, unique, ambig);
	}

	private <I extends GenericEntity & IsIndexed> I indexed(EntityType<I> et, String unique, String ambig) {
		I i = et.create();
		i.putAmbig(ambig);
		i.putUnique(unique);

		return i;
	}

}
