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
package com.braintribe.model.processing.generic.synchronize.test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.synchronization.ExternalId;
import com.braintribe.model.processing.generic.synchronize.test.model.Address;
import com.braintribe.model.processing.generic.synchronize.test.model.Person;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.i18n.I18nTools;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class AbstractEntitySynchronizationTests {

	public AbstractEntitySynchronizationTests() {
	}

	/* ******************************************
	 * Helper methods
	 * ******************************************/

	
	protected void assertUniqueEntityExistsWithProperties(Object object, String[] properties, Object[] values) {
		GenericEntity entity = (GenericEntity) object;
		BtAssertions.assertThat(entity).isNotNull();
		EntityType<GenericEntity> type = entity.entityType();
		for (int i = 0; i < properties.length; i++) {
			BtAssertions.assertThat(getSimplifiedPropertyValue(type.getProperty(properties[i]),entity)).isEqualTo(values[i]);
		}
	}
	
	protected void assertEntityEqual(GenericEntity expected, GenericEntity compare) {
		BtAssertions.assertThat(isEqual(expected, compare)).isTrue();
	}
	
	protected void assertEntitiesEqual(Collection<? extends GenericEntity> expectedElements, Collection<? extends GenericEntity> compareElements) {
		Set<GenericEntity> tmpExpectedElements = new HashSet<GenericEntity>(expectedElements);
		for (GenericEntity c : compareElements) {
			for (GenericEntity e : expectedElements) {
				if (isEqual(e,c)) {
					tmpExpectedElements.remove(e);
				}
			}
		}
		BtAssertions.assertThat(tmpExpectedElements).isEmpty();
	}
	
	protected boolean isEqual(GenericEntity expected, GenericEntity compare) {
		BtAssertions.assertThat(compare).isNotNull();
		BtAssertions.assertThat(compare).isInstanceOf(expected.getClass());

		EntityType<GenericEntity> type = expected.entityType();
		for (Property p : type.getProperties()) {
			if (p.isIdentifier()) {
				continue;
			}
			if (p.isGlobalId()) {
				continue;
			}
			if (p.isPartition()) {
				continue;
			}
			Object expectedValue = getSimplifiedPropertyValue(p,expected);
			Object compareValue = getSimplifiedPropertyValue(type.getProperty(p.getName()),compare);
			if (expectedValue == null) {
				if (compareValue != null) {
					return false;
				}
			} else {
				if (!expectedValue.equals(compareValue)) {
					return false;
				}
				
			}
		}
		return true;
	}

	protected Object getSimplifiedPropertyValue(Property p, GenericEntity entity) {
		Object value = p.get(entity);
		if (value instanceof LocalizedString) {
			value = I18nTools.getDefault((LocalizedString) value);
		}
		else if (value instanceof GenericEntity) {
			value = null;
		}
		return value;
	}
	
	protected <T extends GenericEntity> List<T> query(PersistenceGmSession session, Class<T> clazz) throws GmSessionException {
		return session.query().entities(EntityQueryBuilder.from(clazz).done()).list();
	}

	protected <T extends GenericEntity> List<T> queryCache(PersistenceGmSession session, Class<T> clazz) throws GmSessionException {
		return session.query().entities(EntityQueryBuilder.from(clazz).done()).list();
	}

	protected <T extends GenericEntity> T queryUnique(PersistenceGmSession session, Class<T> clazz) throws GmSessionException {
		return session.query().entities(EntityQueryBuilder.from(clazz).done()).unique();
	}

	protected <T extends GenericEntity> T queryCacheUnique(PersistenceGmSession session, Class<T> clazz) throws GmSessionException {
		return session.queryCache().entities(EntityQueryBuilder.from(clazz).done()).unique();
	}

	protected void configureExternalId(GmMetaModel model, Class<? extends GenericEntity> type, String propertyName) {
		GmProperty property = getProperty(model, type, propertyName);
		property.getMetaData().add(ExternalId.T.create());
	}

	protected GmProperty getProperty(GmMetaModel model, Class<? extends GenericEntity> type, String propertyName) {
		GmEntityType entityType = getEntityType(model, type);
		for (GmProperty property : entityType.getProperties()) {
			if (property.getName().equals(propertyName)) {
				return property;
			}
		}
		throw new RuntimeException("Can't find property: "+propertyName);
	}


	protected GmEntityType getEntityType(GmMetaModel model, Class<? extends GenericEntity> type) {
		for (GmType gmType : model.getTypes() ) {
			if (gmType.getTypeSignature().equals(type.getName())) {
				return (GmEntityType) gmType;
			}
		}
		throw new RuntimeException("Can't find entityType: "+type);
	}
	
	protected Person newDefaultPerson() {
		return newPerson(11111, "John", "Doe", "John Doe description");
	}
	
	protected Address newDefaultAddress() {
		return newAddress("Kandlgasse", 1070, "Vienna");
	}

	protected Person newPerson (Integer ssnr, String firstName, String lastName, String description) {
		Person p =Person.T.create();
		p.setSsnr(ssnr);
		p.setFirstName(firstName);
		p.setLastName(lastName);
		p.setDescription(description);
		return p;
	}

	protected Address newAddress (String street, int zipCode, String city) {
		Address a = Address.T.create();
		a.setStreet(street);
		a.setZipCode(zipCode);
		a.setCity(city);
		return a;
	}

}
