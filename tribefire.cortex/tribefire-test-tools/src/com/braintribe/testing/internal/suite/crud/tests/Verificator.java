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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;

/**
 * Has its own session to an access and helps to verify test results
 */
public class Verificator extends AbstractAccessInspector {
	private PersistenceGmSession session;

	public Verificator(String accessId, PersistenceGmSessionFactory factory) {
		super(accessId, factory);
		this.session = factory.newSession(accessId);
	}

	public void assertEntitiesArePersisted(Collection<GenericEntity> expectedEntities) {
		for (GenericEntity entity : expectedEntities) {
			assertThat(expectedEntityWasInstantiated(entity.entityType(), entity)).isTrue();
		}
	}

	public void assertEntitiesAreNotPresent(Collection<GenericEntity> expectedEntities) {
		for (GenericEntity entity : expectedEntities) {
			GenericEntity foundEntity = session.query().entity(entity).find();
			assertThat(foundEntity).as("Entity was not deleted: %s", foundEntity).isNull();
		}
	}

	private boolean expectedEntityWasInstantiated(EntityType<?> entityType, GenericEntity expectedValue) {

		GenericEntity actualValue = session.query().entity(expectedValue).refresh();

		// ensures that all SIMPLE properties of the entity are as expected
		for (Property property : entityType.getProperties()) {
			if (property.getType().isSimple() && propertyIsNotFiltered(property, actualValue, session)) {
				Object actual = property.get(actualValue);
				Object expected = property.get(expectedValue);

				assertThat(actual).as("Property " + property.getName() + " differs: " + actual + " != " + expected).isEqualTo(expected);
			}
		}

		return true;
	}
	
	
	
	public Iterable<Property> nonFilteredPropertiesOf(GenericEntity entity){
		return nonFilteredPropertiesOf(entity, session);
	}


}
