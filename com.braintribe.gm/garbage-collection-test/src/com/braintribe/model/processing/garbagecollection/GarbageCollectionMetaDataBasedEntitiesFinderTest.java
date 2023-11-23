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
package com.braintribe.model.processing.garbagecollection;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.cleanup.GarbageCollection;
import com.braintribe.model.meta.data.cleanup.GarbageCollectionKind;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.testing.model.test.technical.features.CollectionEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.model.test.testtools.TestModelTestTools;

/**
 * Provides tests for {@link GarbageCollectionMetaDataBasedEntitiesFinder}.
 *
 * @author michael.lafite
 */
public class GarbageCollectionMetaDataBasedEntitiesFinderTest {

	@Test
	public void testFindEntityTypes() {

		final GmMetaModel metaModel = TestModelTestTools.createTestModelMetaModel();

		BasicModelOracle modelOracle = new BasicModelOracle(metaModel);
		
		final GmEntityType genericEntityType = modelOracle.getEntityTypeOracle(GenericEntity.T).asGmEntityType();
		addGarbageCollectionMetaData(genericEntityType, GarbageCollectionKind.hold);

		final GmEntityType complexEntityType = modelOracle.getEntityTypeOracle(ComplexEntity.T).asGmEntityType();
		addGarbageCollectionMetaData(complexEntityType, GarbageCollectionKind.anchor);

		final GmEntityType simpleEntityType = modelOracle.getEntityTypeOracle(SimpleEntity.T).asGmEntityType();
		addGarbageCollectionMetaData(simpleEntityType, GarbageCollectionKind.collect);

		final Set<String> holdTypes = GarbageCollectionMetaDataBasedEntitiesFinder.findEntityTypes(metaModel,
				GarbageCollectionKind.hold, null);
		assertThat(holdTypes).contains(GenericEntity.class.getName());
		// inherited
		assertThat(holdTypes).contains(CollectionEntity.class.getName());
		// other types
		assertThat(holdTypes).doesNotContain(SimpleEntity.class.getName(), ComplexEntity.class.getName());
	}

	private static void addGarbageCollectionMetaData(final GmEntityType entityType,
			final GarbageCollectionKind garbageCollectionKind) {
		final GarbageCollection garbageCollection = GarbageCollection.T.create();
		garbageCollection.setKind(garbageCollectionKind);
		if (entityType.getMetaData() == null) {
			entityType.setMetaData(newSet());
		}
		entityType.getMetaData().add(garbageCollection);
	}
}
