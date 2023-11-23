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
package com.braintribe.model.access.security.cloning;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.model.access.security.query.PostQueryExpertContextImpl;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.TypedCriterion;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.processing.security.query.PostQueryExpertConfiguration;
import com.braintribe.model.processing.security.query.expert.EntityAccessExpert;
import com.braintribe.model.processing.security.query.expert.PostQueryExpert;
import com.braintribe.model.processing.security.query.expert.PropertyRelatedAccessExpert;

/**
 * 
 */
public class IlsExpertsRegistry {

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private final Collection<PostQueryExpertConfiguration> expertConfigurations;

	private final Map<GenericModelType, Collection<PropertyRelatedAccessExpert>> propertyExpertsCache = newCacheMap();
	private final Map<GenericModelType, Collection<EntityAccessExpert>> instanceExpertsCache = newCacheMap();

	private static <K, V> Map<K, V> newCacheMap() {
		return new ConcurrentHashMap<>();
	}

	public IlsExpertsRegistry(Collection<PostQueryExpertConfiguration> expertConfigurations) {
		this.expertConfigurations = newSet();

		this.expertConfigurations.addAll(expertConfigurations);
		this.expertConfigurations.addAll(DefaultIlsConfigurations.get());
	}

	/** Implements the logic behind ILS {@link Matcher}. Must be thread-safe!!! */
	public boolean matches(PostQueryExpertContextImpl ilsExpertContext, TraversingContext tc) {
		switch (tc.getCurrentCriterionType()) {
			case ROOT:
			case LIST_ELEMENT:
			case SET_ELEMENT:
				return matchesEntityOnTop(ilsExpertContext, tc);
			case MAP_ENTRY:
				return matchesMapEntryOnTop(ilsExpertContext, tc);
			case PROPERTY:
				return matchesPropertyOnTop(ilsExpertContext, tc);
			default:
				return false;
		}

	}

	private boolean matchesEntityOnTop(PostQueryExpertContextImpl ilsExpertContext, TraversingContext tc) {
		Object object = tc.getObjectStack().peek();
		GenericModelType type = getType(tc.getTraversingStack().peek());

		return matchesInstance(ilsExpertContext, object, type);
	}

	private static final int KEY = 0;
	private static final int VALUE = 1;

	private boolean matchesMapEntryOnTop(PostQueryExpertContextImpl ilsExpertContext, TraversingContext tc) {
		Map.Entry<?, ?> entry = (Map.Entry<?, ?>) tc.getObjectStack().peek();
		CollectionType type = getType(tc.getTraversingStack().peek());

		GenericModelType[] entryTypes = type.getParameterization();

		return matchesInstance(ilsExpertContext, entry.getKey(), entryTypes[KEY])
				|| matchesInstance(ilsExpertContext, entry.getValue(), entryTypes[VALUE]);
	}

	private boolean matchesPropertyOnTop(PostQueryExpertContextImpl ilsExpertContext, TraversingContext tc) {
		GenericEntity entity = (GenericEntity) getPropertyOwner(tc.getObjectStack());
		if (entity.getId() == null)
			return false;

		EntityType<?> entityType = (EntityType<?>) getType(getPropertyOwner(tc.getTraversingStack()));

		PropertyCriterion pc = (PropertyCriterion) tc.getTraversingStack().peek();
		Object propertyValue = tc.getObjectStack().peek();
		String propertyName = pc.getPropertyName();
		GenericModelType propertyType = getType(pc);

		Property property = entityType.getProperty(propertyName);

		PropertyPathElement propertyPathElement = new PropertyPathElement(entity, property, propertyValue);

		if (matchesProperty(ilsExpertContext, propertyPathElement, propertyType))
			return true;

		return matchesInstance(ilsExpertContext, propertyValue, propertyType);
	}

	private static <T> T getPropertyOwner(Stack<T> objectStack) {
		return objectStack.get(objectStack.size() - 2);
	}

	private <T extends GenericModelType> T getType(BasicCriterion criterion) {
		String typeSignature = ((TypedCriterion) criterion).getTypeSignature();
		return (T) typeReflection.getType(typeSignature);
	}

	private boolean matchesInstance(PostQueryExpertContextImpl ilsExpertContext, Object instance, GenericModelType type) {
		if (!(instance instanceof GenericEntity))
			return false;

		GenericEntity entity = (GenericEntity) instance;
		if (entity.getId() == null)
			// this is a container entity, we do not want to check this
			return false;

		ilsExpertContext.setEntity((GenericEntity) instance);

		for (EntityAccessExpert expert : acquireEntityExpertsFor(type))
			if (!expert.isAccessGranted(ilsExpertContext))
				return true;

		return false;
	}

	private boolean matchesProperty(PostQueryExpertContextImpl ilsExpertContext, PropertyPathElement ppe, GenericModelType propertyType) {
		ilsExpertContext.setPropertyRelatedModelPathElement(ppe);

		for (PropertyRelatedAccessExpert expert : acquirePropertyExpertsFor(propertyType))
			if (!expert.isAccessGranted(ilsExpertContext))
				return true;

		return false;
	}

	private Collection<PropertyRelatedAccessExpert> acquirePropertyExpertsFor(GenericModelType propertyType) {
		return acquireExpertsFor(PropertyRelatedAccessExpert.class, propertyType, propertyExpertsCache);
	}

	private Collection<EntityAccessExpert> acquireEntityExpertsFor(GenericModelType type) {
		return acquireExpertsFor(EntityAccessExpert.class, type, instanceExpertsCache);
	}

	private <T extends PostQueryExpert> Collection<T> acquireExpertsFor(Class<T> clazz, GenericModelType type,
			Map<GenericModelType, Collection<T>> cache) {

		return cache.computeIfAbsent(type, t -> getMatchingExperts(clazz, type));
	}

	private <T extends PostQueryExpert> Collection<T> getMatchingExperts(Class<T> clazz, GenericModelType type) {
		List<T> result = newList();

		for (PostQueryExpertConfiguration config : expertConfigurations) {
			PostQueryExpert expert = config.getExpert();

			if (clazz.isInstance(expert) && config.getTypeCondition().matches(type))
				result.add(clazz.cast(expert));
		}

		return result;
	}

}
