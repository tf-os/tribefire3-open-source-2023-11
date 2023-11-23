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
package com.braintribe.testing.internal.suite.crud.tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.testing.internal.suite.crud.GenericModelInstantiator;
import com.braintribe.utils.CommonTools;

/**
 * See {@link #run}
 *
 */
public class CreateEntitiesTest extends AbstractAccessCRUDTest {

	public CreateEntitiesTest(String accessId, PersistenceGmSessionFactory factory) {
		super(accessId, factory);
		testedEntityTypes = new HashSet<>();

	}

	private Set<EntityType<?>> testedEntityTypes;

	public void testOnlyEntityTypesDirectlyDeclaredByModels(GmMetaModel... models) {
		testedEntityTypes.clear();
		if (models.length == 0) {
			throw new IllegalArgumentException("No null arguments allowed in 'CreateEntitiesTest.testOnlyEntityTypesDirectlyDeclaredByModels()'");
		} else {
			for (GmMetaModel model : models) {
				testedEntityTypes.addAll(
						model.entityTypes().map(x -> GMF.getTypeReflection().getEntityType(x.getTypeSignature())).collect(Collectors.toSet()));
			}
		}
	}

	public void setTestedEntityTypes(Set<EntityType<?>> testedEntityTypes) {
		this.testedEntityTypes = testedEntityTypes;
	}

	public void setTestedEntityTypes(EntityType<?>... testedEntityTypes) {

		setTestedEntityTypes(CommonTools.toSet(testedEntityTypes));
	}

	/**
	 * If you don't set anything: creates one entity of each possible entity type <br>
	 * If you call one of the two setters first you can reduce the number of tested entity types <br>
	 * Verification step: checks if created entities can be found by query
	 *
	 * @return created entities
	 */
	@Override
	public List<GenericEntity> run(PersistenceGmSession session) {
		GenericModelInstantiator gmi;
		if (testedEntityTypes == null) {
			gmi = new GenericModelInstantiator(session);
		} else {
			gmi = new GenericModelInstantiator(session, testedEntityTypes);
		}

		gmi.setPropertyFilter(getFilterPredicate());
		gmi.start();
		return new ArrayList<GenericEntity>(gmi.getCachedEntities());
	}

	@Override
	protected void verifyResult(Verificator verificator, List<GenericEntity> testResult) {
		verificator.assertEntitiesArePersisted(testResult);
	}

}
