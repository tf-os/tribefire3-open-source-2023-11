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
package com.braintribe.model.access.smart.test.manipulation;

import java.util.List;

import org.junit.Before;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.smart.test.base.AbstractSmartAccessTests;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeIkpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.EnumEntityB;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonB;
import com.braintribe.model.processing.query.smart.test.model.accessB.StandardIdEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeIkpaEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeKpaEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.Id2UniqueEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartEnumEntityB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartGenericEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartStringIdEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartManualA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartReaderA;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.SelectQuery;

public class AbstractManipulationsTests extends AbstractSmartAccessTests {

	protected PersistenceGmSession session;

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();

		session = new BasicPersistenceGmSession(smartAccess);
	}

	protected void commit() {
		try {
			session.commit();
		} catch (GmSessionException e) {
			throw new RuntimeException("[TEST] Session commmit failed.", e);
		}
	}

	// ###################################
	// ## . . . . Creating Data . . . . ##
	// ###################################

	protected final SmartPersonA newSmartPersonA() {
		return newEntity(SmartPersonA.T);
	}

	protected final SmartPersonB newSmartPersonB() {
		return newEntity(SmartPersonB.T);
	}

	protected final Company newCompany() {
		return newEntity(Company.T);
	}

	protected final SmartItem newSmartItem() {
		return newEntity(SmartItem.T);
	}

	protected final SmartEnumEntityB newSmartEnumEntityB() {
		return newEntity(SmartEnumEntityB.T);
	}

	protected final SmartStringIdEntity newStandardStringIdEntity() {
		return newEntity(SmartStringIdEntity.T);
	}

	protected final Id2UniqueEntity newId2UniqueEntity() {
		return newEntity(Id2UniqueEntity.T);
	}

	protected final CompositeKpaEntity newCompositeKpaEntity() {
		return newEntity(CompositeKpaEntity.T);
	}

	protected final CompositeIkpaEntity newCompositeIkpaEntity() {
		return newEntity(CompositeIkpaEntity.T);
	}

	protected final SmartReaderA newSmartReaderA() {
		return newEntity(SmartReaderA.T);
	}

	protected final SmartBookB newSmartBookB() {
		return newEntity(SmartBookB.T);
	}

	protected final SmartManualA newSmartManualA() {
		return newEntity(SmartManualA.T);
	}
	
	protected <T extends SmartGenericEntity> T newEntity(EntityType<T> entityType) {
		return session.create(entityType);
	}

	// ###################################
	// ## . . Querying Delegate Date . .##
	// ###################################

	protected PersonA personAByName(String name) {
		return selectByProperty(PersonA.class, "nameA", name, smoodA);
	}

	protected ItemB itemBByName(String name) {
		return selectByProperty(ItemB.class, "nameB", name, smoodB);
	}

	protected CompositeIkpaEntityA compositeIkpaEntityAByDescription(String description) {
		return selectByProperty(CompositeIkpaEntityA.class, "description", description, smoodA);
	}

	protected StandardIdEntity standardIdEntityByName(String name) {
		return selectByProperty(StandardIdEntity.class, "name", name, smoodB);
	}

	protected <T extends GenericEntity> T selectByProperty(Class<T> clazz, String propertyName, Object propertyValue,
			IncrementalAccess access) {

		return single(new SelectQueryBuilder().from(clazz, "e").select("e").where().property("e", propertyName).eq(propertyValue).done(),
				access);
	}

	@SuppressWarnings("unchecked")
	protected <T> T single(SelectQuery query, IncrementalAccess access) {
		try {
			return (T) access.query(query).getResults().get(0);

		} catch (Exception e) {
			throw new RuntimeException("Query evaluation failed for: " + access, e);
		}
	}

	protected <T extends GenericEntity> List<T> listAllByProperty(Class<T> clazz, String propertyName, Object propertyValue,
			IncrementalAccess access) {
		SelectQuery query = new SelectQueryBuilder().from(clazz, "e").select("e").where().property("e", propertyName).eq(propertyValue)
				.done();

		try {
			return cast(access.query(query).getResults());

		} catch (Exception e) {
			throw new RuntimeException("Query evaluation failed for: " + smoodB, e);
		}
	}

	protected long countPersonA() {
		return count(PersonA.class, smoodA);
	}

	protected long countPersonB() {
		return count(PersonB.class, smoodB);
	}

	protected long countCompanyA() {
		return count(CompanyA.class, smoodA);
	}

	protected long countItemB() {
		return count(ItemB.class, smoodB);
	}

	protected long countEnumEntityB() {
		return count(EnumEntityB.class, smoodB);
	}

	protected long count(Class<? extends GenericEntity> clazz, Smood database) {
		return database.getEntitiesPerType(GMF.getTypeReflection().getEntityType(clazz)).size();
	}

	@SuppressWarnings("unchecked")
	public static <T> T cast(Object o) {
		return (T) o;
	}
}
