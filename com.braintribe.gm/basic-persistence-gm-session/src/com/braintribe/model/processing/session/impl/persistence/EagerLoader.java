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
package com.braintribe.model.processing.session.impl.persistence;

import static com.braintribe.utils.lcd.CollectionTools2.splitToSets;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.processing.query.support.BulkPropertyQueryTools;
import com.braintribe.model.processing.session.api.notifying.interceptors.CollectionEnhancer;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

/**
 * {@link PropertyAccessInterceptor} which is like an eager version of a {@link LazyLoader}. When accessing a property,
 * it checks for it being absent, and if so, it leads this property for every single entity of compatible type (i.e.
 * this type and sub-types) currently attached to a session.
 * <p>
 * IMPORTANT NOTE: This PAI has to be configured after the {@link CollectionEnhancer} in the PAI list, because
 * {@link CollectionEnhancer} would replace the {@link AbsenceInformation} with an absent collection, and this PAI would
 * the not consider the property as absent.
 * <p>
 * NOTE: Not all property values are retrieved via a single query, but the query is split into bulks, where one bulk can
 * have the maximum of {@link #bulkSize} entities (owners of given property).
 * <p>
 * NOTE: In case the loaded property value contains entities of a type compatible to the root entity, i.e. such entities
 * where we could again apply the eager loading, we do not do that. In such case loading would be triggered the first
 * time this property is accessed for one of these new entities.
 * 
 * @author peter.gazdik
 */
public class EagerLoader extends PropertyAccessInterceptor {

	// It makes sense to use a number not bigger than EvalDelegateQueryJoin.bulkSize
	public static final int DEFAULT_BULK_SIZE = 100;

	private final EagerLoaderSupportingAccess access;
	private final PersistenceGmSession session;
	private final Function<EntityType<?>, ? extends Collection<? extends GenericEntity>> populationProvider;

	private int bulkSize = DEFAULT_BULK_SIZE;

	public EagerLoader(EagerLoaderSupportingAccess access, AbstractPersistenceGmSession session) {
		this(access, session, et -> session.getBackup().getEntitiesPerType(et));
	}

	public EagerLoader(EagerLoaderSupportingAccess access, PersistenceGmSession session,
			Function<EntityType<?>, ? extends Collection<? extends GenericEntity>> populationProvider) {

		this.access = access;
		this.session = session;
		this.populationProvider = populationProvider;
	}

	public void setBulkSize(int bulkSize) {
		this.bulkSize = bulkSize;
	}

	@Override
	public Object getProperty(Property property, GenericEntity entity, boolean isVd) {
		if (isVd)
			throw new GenericModelException("Resolving VDs is not expected here.");

		Object value = next.getProperty(property, entity, false);
		if (VdHolder.isVdHolder(value)) {
			VdHolder.checkIsAbsenceInfo(value, entity, property);

			if (property.isIdentifying())
				throw new GenericModelException(
						"This entity was not initialized properly, as one of it's identity properties (" + property.getName() + ") is absent.");

			try {
				eagerlyLoad(entity, property);

			} catch (Exception e) {
				throw new GenericModelException("error while resolving absent property: " + property.getName(), e);
			}

			value = next.getProperty(property, entity, false);
		}
		return value;
	}

	/**
	 * @param entity
	 *            - entity which triggered the eager loading. The instance can be used if this method is overridden, to
	 *            check if we really want to do eager loading there.
	 */
	protected void eagerlyLoad(GenericEntity entity, Property property) {
		Set<GenericEntity> allOwnersToLoad = findOwnersWhereAbsentIs(property);

		List<Set<GenericEntity>> ownersBulks = splitToSets(allOwnersToLoad, bulkSize);

		for (Set<GenericEntity> ownersToLoad : ownersBulks)
			eagerlyLoadBulk(property, ownersToLoad);
	}

	protected void eagerlyLoadBulk(Property property, Set<GenericEntity> ownersToLoad) {
		SelectQuery selectQuery = BulkPropertyQueryTools.buildQueryForProperty(ownersToLoad, property);

		SelectQueryResult sqResult = access.query(selectQuery, session);

		Map<GenericEntity, Object> ownerToPropertyValue = BulkPropertyQueryTools.buildPropertyMap(ownersToLoad, property, sqResult);

		setPropertyValues(property, ownersToLoad, ownerToPropertyValue);
	}

	// Yoda style
	private Set<GenericEntity> findOwnersWhereAbsentIs(Property property) {
		EntityType<?> ownerType = property.getDeclaringType();

		Collection<? extends GenericEntity> population = populationProvider.apply(ownerType);

		return population.stream() //
				.filter(owner -> isCandidateForEagerLoader(owner, property)) //
				.collect(Collectors.toSet());
	}

	/**
	 * Default implementation just checks if the property is absent. This might be overridden in some Access to check,
	 * for example, if the property is also mapped for given entity.
	 */
	protected boolean isCandidateForEagerLoader(GenericEntity owner, Property property) {
		Object fieldValue = property.getDirectUnsafe(owner);
		return VdHolder.isVdHolder(fieldValue) && ((VdHolder) fieldValue).isAbsenceInformation;
	}

	protected void setPropertyValues(Property property, Set<GenericEntity> ownersToLoad, Map<GenericEntity, Object> ownerToPropertyValue) {
		session.suspendHistory();
		try {
			setPropertyValuesWhenHistorySuspended(property, ownersToLoad, ownerToPropertyValue);

		} finally {
			session.resumeHistory();
		}
	}

	protected void setPropertyValuesWhenHistorySuspended(Property property, Set<GenericEntity> ownersToLoad,
			Map<GenericEntity, Object> ownerToPropertyValue) {
		for (Entry<GenericEntity, Object> entry : ownerToPropertyValue.entrySet()) {
			GenericEntity owner = entry.getKey();
			Object propertyValue = entry.getValue();

			next.setProperty(property, owner, propertyValue, false);

			ownersToLoad.remove(owner);
		}

		for (GenericEntity ownerWithEmptyValue : ownersToLoad)
			next.setProperty(property, ownerWithEmptyValue, null, false);
	}

}
