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
package com.braintribe.product.rat.imp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.lcd.CommonTools;

/**
 * Abstract base class for ImpCaves.
 * <p>
 * An ImpCave always belongs to a certain {@link EntityType} and is responsible for creating an {@link AbstractImp imp}
 * that manages an entity instance of that type, as well as finding an instance by its typical identification property.
 * Depending on the {@link EntityType} of the instance, the typical identification property varies. For example a
 * GmMetaModel is found by its <code>name</code> property, a Deployable by its <code>externalId</code>.
 *
 * @param <T>
 *            Type of the instance that will be managed by the imp ({@link #find(String)}, {@link #with(GenericEntity)},
 *            ...)
 * @param <I>
 *            Type of the imp that will manage the instance of type T ({@link #with(GenericEntity)}, ...)
 */
public abstract class AbstractImpCave<T extends GenericEntity, I extends Imp<T>> extends AbstractHasSession {

	protected final String identificationPropertyName;
	protected final EntityType<T> typeOfT;

	public AbstractImpCave(PersistenceGmSession session, String identificationPropertyName, EntityType<T> typeOfT) {
		super(session);
		this.identificationPropertyName = identificationPropertyName;
		this.typeOfT = typeOfT;
	}

	/**
	 * finds a unique entity<br>
	 * i.e. find(HibernateAccess.T, "test.access");
	 *
	 * @param entityType
	 *            type of entity to find
	 * @param identification
	 *            the typical identification string (i.e. the name for a model or the externalId for a deployable)
	 * @return the found unique instance or null
	 */
	public <E extends T> Optional<E> find(EntityType<E> entityType, String identification) {
		if (entityType == null) {
			entityType = (EntityType<E>) typeOfT;
		}

		return Optional.ofNullable(queryHelper.entityWithProperty(entityType, identificationPropertyName, identification));
	}

	/**
	 * i.e. findAll(HardwiredAccess.T, "cortex*") or findAll(GmEnumType.T, "*Driver")
	 *
	 * @param identificationPattern
	 *            a pattern (like you would use for a 'like' operator for a query) for the typical identification string
	 *            (i.e. the name for a model or the externalId for a deployable)
	 * @return a list of found instances or an empty list
	 */
	public <E extends T> List<E> findAll(EntityType<E> entityType, String identificationPattern) {
		if (entityType == null) {
			entityType = (EntityType<E>) typeOfT;
		}

		return new ArrayList<>(queryHelper.entitiesWithPropertyLike(entityType, identificationPropertyName, identificationPattern));
	}

	/**
	 * finds a unique entity<br>
	 * i.e. find("com.braintribe.gm:root-model")
	 *
	 * @param identification
	 *            the typical identification string (i.e. the name for a model or the externalId for a deployable)
	 * @return an {@link Optional} for the found unique instance
	 */
	public Optional<T> find(String identification) {
		return find(null, identification);
	}

	/**
	 * i.e. findAll("cortex*") or findAll("*:RootModel")
	 *
	 * @param identificationRegex
	 *            a pattern (like you would use for a 'like' operator for a query) for the typical identification string
	 *            (i.e. the name for a model or the externalId for a deployable)
	 * @return a list of found instances or an empty list
	 */
	public List<T> findAll(String identificationRegex) {
		return findAll(null, identificationRegex);
	}

	/**
	 * Allows an imp cave to provide additional validation. Is called during {@link #with(GenericEntity)}
	 */
	@SuppressWarnings("unused")
	protected void validate(T instance) {
		// empty default implementation
	}

	/**
	 * Creates a context-specific imp, retrieving its instance via {@link #find(String)}
	 *
	 * @param identificationString
	 *            {@link #find(String)}
	 * @return the newly created imp
	 */
	public I with(String identificationString) {
		String errorMsg = "Could not find suitable instance with " + identificationPropertyName + " set to '" + identificationString
				+ "'. Either it does not exist or it was not committed yet";

		T foundInstance = find(identificationString).orElseThrow(() -> new ImpException(errorMsg));

		return with(foundInstance);
	}

	protected abstract I buildImp(T instance);

	/**
	 * Creates a context-specific imp from provided instance
	 *
	 * @param instance
	 *            the instace of the imp that should be created
	 * @return the newly created imp
	 * @throws ImpException
	 *             if the passed instance is null or attached to a different session than this ImpCave's session
	 */
	public <E extends T> I with(E instance) {
		if (instance == null) {
			throw new ImpException("Cannot create imp with null pointer");
		}

		if (instance.session() != session()) {
			throw new ImpException(
					"The instance you passed is from another session. Please only use entities from this imp's session. To be safe you can make sure to use only instances created by the very same imp.");
		}

		validate(instance);

		return buildImp(instance);
	}

	/**
	 * Creates a context-specific {@link GenericMultiImp}, retrieving its instances via {@link #find(String)}
	 *
	 * @return the newly created multi-imp
	 * @throws ImpException
	 *             if no instance could be found for at least one of the provided identificationStrings
	 */
	public GenericMultiImp<T, I> with(String... identificationStrings) {
		Set<String> notFoundIds = new HashSet<>();
		List<T> foundInstances = new ArrayList<>();

		for (String id : identificationStrings) {

			Optional<T> foundInstance = find(id);

			if (!foundInstance.isPresent()) {
				notFoundIds.add(id);
			}

			if (notFoundIds.isEmpty()) {
				foundInstances.add(foundInstance.get());
			}
		}

		if (!notFoundIds.isEmpty()) {
			throw new ImpException("Could not find instances with ids: " + notFoundIds);
		}

		return with(foundInstances);
	}

	/**
	 * Creates a context-specific {@link GenericMultiImp} using provided instances
	 *
	 * @return the newly created multi-imp
	 * @throws ImpException
	 *             if one of the passed instances is null
	 */
	public <E extends T> GenericMultiImp<T, I> with(E... instances) {
		return with(CommonTools.toCollection(instances));
	}

	/**
	 * Creates a context-specific {@link GenericMultiImp} using provided instances
	 *
	 * @return the newly created multi-imp
	 * @throws ImpException
	 *             if one of the passed instances is null
	 */
	public <E extends T> GenericMultiImp<T, I> with(Collection<E> collection) {
		Collection<I> impsToUse = impify(collection);

		return new GenericMultiImp<T, I>(session(), impsToUse);
	}

	/**
	 * helper for building a multi-imp from multiple instances
	 *
	 * @param collection
	 *            collection of instances to be used for a multi-imp
	 * @return collection of imps to be used for a multi-imp
	 * @throws ImpException
	 *             if one of the passed instances is null
	 */
	protected <E extends T> Collection<I> impify(Collection<E> collection) {
		if (collection == null || collection.contains(null)) {
			throw new ImpException("Can't create multi-imp! One of your parameters was or did resolve to a null pointer");
		}

		return collection.stream().map(this::with).collect(Collectors.toList());
	}

	/**
	 * i.e. allLike("com.mycompany.*") <br>
	 * <br>
	 *
	 * Creates a context-specific {@link GenericMultiImp}, retrieving its instances via {@link #findAll(String)}
	 *
	 * @return the newly created multi-imp
	 */
	public GenericMultiImp<T, I> allLike(String identificationPattern) {
		return with(findAll(identificationPattern));
	}

	/**
	 * Creates a context-specific {@link GenericMultiImp} with all instances it can find
	 *
	 * @return the newly created multi-imp
	 */
	public GenericMultiImp<T, I> all() {
		return allLike("*");
	}
}
