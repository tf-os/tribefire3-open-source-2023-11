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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.util.Arrays;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.test.model.indexed.IndexedA;
import com.braintribe.model.processing.query.test.model.indexed.IndexedAB;
import com.braintribe.model.processing.query.test.model.indexed.IndexedB;
import com.braintribe.model.processing.query.test.model.indexed.IsIndexed;
import com.braintribe.model.processing.smood.population.SmoodIndexTools;
import com.braintribe.model.processing.smood.test.AbstractSmoodTests;

/**
 * 
 */
public class Smood_Index_Hierarchy_Metric_Test extends AbstractSmoodTests {

	// ###################################################
	// ## . . . . . . . Indices are there . . . . . . . ##
	// ###################################################

	@Test
	public void indicesAreThere() {
		registerAtSmood(indexedAB("uniqueAB", "ambigAB"));
		assertHasBothIndices(IndexedA.T);
		assertHasBothIndices(IndexedB.T);
		assertHasBothIndices(IndexedAB.T);
	}

	private void assertHasBothIndices(EntityType<?> et) {
		String ts = et.getTypeSignature();
		assertThat(smood.provideIndexInfo(ts, IndexedA.metric)).isNotNull();
		assertThat(smood.provideIndexInfo(ts, IndexedA.ambig)).isNotNull();
	}

	// ###################################################
	// ## . . . . . . . . Lookup works . . . . . . . . .##
	// ###################################################

	/** We test that all levels relevant for IndexedAB (i.e. super-types) are able to resolve the entity for given value. */
	@Test
	public void metric_noMergeNeeded() {
		registerAtSmood(indexedAB("aa", "ambig"));
		registerAtSmood(indexedAB("bb", "ambig"));
		registerAtSmood(indexedAB("cc", "ambig"));
		registerAtSmood(indexedAB("dd", "ambig"));

		check_BD_Range(IndexedAB.T, "bb", "cc");
		check_BD_Range(IndexedA.T, "bb", "cc");
		check_BD_Range(IndexedB.T, "bb", "cc");

		check_Full_Range(IndexedAB.T, "aa", "bb", "cc", "dd");
		check_Full_Range(IndexedA.T, "aa", "bb", "cc", "dd");
		check_Full_Range(IndexedB.T, "aa", "bb", "cc", "dd");
	}

	@Test
	public void metric_mergeNeeded() {
		registerAtSmood(indexedA("aA", "ambig"));
		registerAtSmood(indexedAB("aAB", "ambig"));

		registerAtSmood(indexedA("bA", "ambig"));
		registerAtSmood(indexedA("cA", "ambig"));

		registerAtSmood(indexedAB("bAB", "ambig"));
		registerAtSmood(indexedAB("cAB", "ambig"));

		registerAtSmood(indexedA("dA", "ambig"));

		check_BD_Range(IndexedAB.T, "bAB", "cAB");
		check_BD_Range(IndexedA.T, "bA", "bAB", "cA", "cAB");

		check_Full_Range(IndexedAB.T, "aAB", "bAB", "cAB");
		check_Full_Range(IndexedA.T, "aA", "aAB", "bA", "bAB", "cA", "cAB", "dA");
	}

	@Test
	public void metric_outOfRange() {
		registerAtSmood(indexedAB("gg", "ambig"));
		registerAtSmood(indexedAB("hh", "ambig"));

		check_BD_Range(IndexedAB.T);
		check_BD_Range(IndexedA.T);
		check_BD_Range(IndexedB.T);
	}

	private void check_BD_Range(EntityType<?> et, String... expected) {
		Collection<? extends GenericEntity> entities = smood.getIndexRange(SmoodIndexTools.indexId(et, IndexedA.metric), "b", true, "d", false);
		assertThat(metricValues(entities)).containsExactly((Object[]) expected);
	}

	private void check_Full_Range(EntityType<?> et, String... expected) {
		Collection<? extends GenericEntity> entities = smood.getFullRange(SmoodIndexTools.indexId(et, IndexedA.metric), false);
		assertThat(metricValues(entities)).containsExactly((Object[]) expected);

		Collection<? extends GenericEntity> rEntities = smood.getFullRange(SmoodIndexTools.indexId(et, IndexedA.metric), true);
		assertThat(metricValues(rEntities)).containsExactly(reverseOrder(expected));
	}

	private Object[] reverseOrder(String[] array) {
		List<Object> list = Arrays.asList(array);
		Collections.reverse(list);
		return list.toArray();
	}

	private List<String> metricValues(Collection<? extends GenericEntity> entities) {
		return entities.stream() //
				.map(IndexedA.class::cast) //
				.map(IndexedA::getMetric) //
				.collect(Collectors.toList());
	}

	// ###################################################
	// ## . . . . . . . . . Helpers . . . . . . . . . . ##
	// ###################################################

	private IndexedA indexedA(String metric, String ambig) {
		return indexed(IndexedA.T, metric, ambig);
	}

	private IndexedAB indexedAB(String metric, String ambig) {
		return indexed(IndexedAB.T, metric, ambig);
	}

	private <I extends GenericEntity & IsIndexed> I indexed(EntityType<I> et, String metric, String ambig) {
		I i = et.create();
		i.putAmbig(ambig);
		i.putMetric(metric);

		return i;
	}

}
