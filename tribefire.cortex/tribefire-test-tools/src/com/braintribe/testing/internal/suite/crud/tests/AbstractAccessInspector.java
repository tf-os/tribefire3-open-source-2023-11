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

import static org.junit.Assert.assertNotNull;

import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.testing.internal.suite.crud.PropertyFilterPredicate;

/**
 * Something that holds an accessId and should be able to create a session to this access, as well as ignore certain
 * properties during its work
 * 
 * @author Neidhart
 *
 */
public class AbstractAccessInspector {

	protected final PersistenceGmSessionFactory sessionFactory;
	protected final String accessId;
	protected PropertyFilterPredicate propertyFilterPredicate;

	public AbstractAccessInspector(String accessId, PersistenceGmSessionFactory factory) {
		this.sessionFactory = factory;
		this.accessId = accessId;
		this.propertyFilterPredicate = this::defaultSkipPropertyPredicate;

	}

	public PropertyFilterPredicate getFilterPredicate() {
		return propertyFilterPredicate;
	}

	public void setFilterPredicate(PropertyFilterPredicate filterPredicate) {
		assertNotNull(filterPredicate);
		this.propertyFilterPredicate = filterPredicate;
	}

	public boolean propertyIsNotFiltered(Property property, GenericEntity entity, PersistenceGmSession session) {
		return propertyFilterPredicate.test(property, entity, session);
	}
	
	public Iterable<Property> nonFilteredPropertiesOf(GenericEntity entity, PersistenceGmSession session){
		return entity.entityType()
				.getProperties()
				.stream()
				.filter(prop -> propertyIsNotFiltered(prop, entity, session))
				.collect(Collectors.toList());
	}

	/**
	 * skips inherited properties except when they are mandatory
	 */
	protected boolean defaultSkipPropertyPredicate(Property property, GenericEntity entity, PersistenceGmSession session) {
		PropertyMdResolver propertyMeta = session.getModelAccessory().getMetaData().entity(entity).property(property);
		boolean relevantType = property.getDeclaringType().equals(entity.entityType()) || propertyMeta.is(Mandatory.T);

		return relevantType;

	}

}
