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
package com.braintribe.model.access.hibernate;

import static com.braintribe.model.access.hibernate.HibernateAccessTools.deproxy;

import java.sql.Timestamp;
import java.util.Date;

import com.braintribe.model.access.AbstractAccess.AbstractQueryResultCloningContext;
import com.braintribe.model.access.hibernate.gm.CompositeIdValues;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.traversing.engine.impl.clone.legacy.GmtCompatibleCloningContext;

public class HibernateCloningContext extends AbstractQueryResultCloningContext implements GmtCompatibleCloningContext {

	private final String defaultPartition;

	public HibernateCloningContext(String defaultPartition) {
		this.defaultPartition = defaultPartition;
	}

	@Override
	protected String defaultPartition() {
		return defaultPartition;
	}

	@Override
	public GenericEntity preProcessInstanceToBeCloned(GenericEntity instanceToBeCloned) {
		GenericEntity result = deproxy(instanceToBeCloned);
		ensureIdIsGmValue(result);
		return result;
	}

	/**
	 * In case of a composite ID, we use {@link CompositeIdValues} in the Hibernate mappings, which is not a GM value
	 * (let alone a scalar value) so it has to be adjusted.
	 */
	private void ensureIdIsGmValue(GenericEntity entity) {
		Object id = entity.getId();
		if (id instanceof CompositeIdValues) {
			CompositeIdValues compositeId = (CompositeIdValues) id;
			entity.setId(compositeId.encodeAsString());
		}
	}

	@Override
	public Object postProcessCloneValue(GenericModelType propertyType, Object clonedValue) {
		if (clonedValue instanceof Timestamp)
			return new Date(((Timestamp) clonedValue).getTime());
		else
			return super.postProcessCloneValue(propertyType, clonedValue);
	}

}
