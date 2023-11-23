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
package com.braintribe.model.access.impls;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.api.LookupIndex;
import com.braintribe.model.access.model.Book;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.conditions.Condition;

/**
 * 
 */
public class IndexingAccess extends AbstractTestAccess {

	private final Smood dataSource;

	public IndexingAccess(Smood dataSource, boolean idIsIndexed) {
		this.dataSource = dataSource;

		this.repository.setAssumeIdIsIndexed(idIsIndexed);

		registerIndex(Book.class, "title");
		registerIndex(Book.class, "library");
	}

	private void registerIndex(Class<? extends StandardIdentifiable> clazz, String property) {
		repository.registerIndex(clazz.getName(), property, new EntityPropertyIndex(clazz, property));
	}

	@Override
	protected List<GenericEntity> queryPopulation(String typeSignature, Condition condition) throws ModelAccessException {
		Restriction r = Restriction.T.create();
		r.setCondition(condition);

		EntityQuery query = EntityQueryBuilder.from(typeSignature).done();
		query.setRestriction(r);

		return dataSource.queryEntities(query).getEntities();
	}

	@Override
	protected GenericEntity getEntity(EntityReference entityReference) throws ModelAccessException {
		return dataSource.getEntity(entityReference);
	}

	class EntityPropertyIndex implements LookupIndex {

		private final Class<? extends StandardIdentifiable> clazz;
		private final String propertyName;

		public EntityPropertyIndex(Class<? extends StandardIdentifiable> clazz, String propertyName) {
			this.clazz = clazz;
			this.propertyName = propertyName;
		}

		@Override
		public Collection<? extends GenericEntity> getAllValuesForIndices(Collection<?> indexValues) {
			Set<GenericEntity> result = new HashSet<GenericEntity>();

			for (Object value: indexValues) {
				result.addAll(getAllValuesForIndex(value));
			}

			return result;
		}

		@Override
		public GenericEntity getValueForIndex(Object indexValue) {
			Collection<? extends GenericEntity> all = getAllValuesForIndex(indexValue);

			return all.isEmpty() ? null : all.iterator().next();
		}

		@Override
		public Collection<? extends GenericEntity> getAllValuesForIndex(Object indexValue) {
			EntityQuery query = EntityQueryBuilder.from(clazz).where().property(propertyName).eq().value(indexValue).done();

			try {
				return dataSource.queryEntities(query).getEntities();

			} catch (ModelAccessException e) {
				throw new RuntimeException("Things went south.", e);
			}
		}

	}
}
