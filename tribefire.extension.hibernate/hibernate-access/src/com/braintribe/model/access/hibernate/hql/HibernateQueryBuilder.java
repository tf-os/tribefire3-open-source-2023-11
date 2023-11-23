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
package com.braintribe.model.access.hibernate.hql;

import java.util.Collection;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.type.Type;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.persistence.NativeQueryParameter;

/**
 * @author peter.gazdik
 */
public class HibernateQueryBuilder<T> {

	private final Session session;
	private final Query<T> result;

	public HibernateQueryBuilder(Session session, String hql) {
		this.session = session;
		this.result = session.createQuery(hql);
	}

	public void setPagination(Integer maxResults, Integer firstResult) {
		if (maxResults != null && firstResult != null)
			setPagination(maxResults.intValue(), firstResult.intValue());
	}

	public void setPagination(int maxResults, int firstResult) {
		if (maxResults > 0)
			result.setMaxResults(maxResults);
		result.setFirstResult(firstResult);
	}

	public void setParameter(NativeQueryParameter parameter) {
		setParameter(parameter.getName(), parameter.getValue());
	}

	public void setParameter(String name, Object value) {
		if (value instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) value;
			result.setParameterList(name, collection);

		} else if (value instanceof GenericEntity) {
			GenericEntity entity = (GenericEntity) value;
			Class<?> entityClass = entity.entityType().getJavaType();
			Type hibernateType = session.getSessionFactory().getTypeHelper().entity(entityClass);
			result.setParameter(name, value, hibernateType);

		} else {
			result.setParameter(name, value);
		}
	}

	public Query<T> getResult() {
		return result;
	}

}
