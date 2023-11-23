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
package com.braintribe.model.access.smart.manipulation.tools;

import static com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor.USE_CASE;
import static com.braintribe.model.access.smart.manipulation.tools.SmartManipulationTools.newRefMap;
import static com.braintribe.utils.lcd.CollectionTools2.acquireMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.updateMapKey;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smart.SmartAccess;
import com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor;
import com.braintribe.model.access.smart.query.fluent.SmartSelectQueryBuilder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.smart.SmartAccessException;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.smart.processing.SmartQueryEvaluatorRuntimeException;
import com.braintribe.model.record.ListRecord;

/**
 * This class keeps track of current value of simple/enum properties for smart entities.
 * 
 * @see #notifyChangeValue(EntityReference, String, String, Object)
 * @see #acquireDelegatePropertyValue(EntityReference, String)
 */
public class PropertyValueResolver {

	private final SmartManipulationProcessor manipulationProcessor;
	private final SmartAccess smartAccess;
	private final ModelExpert modelExpert;
	private final AccessResolver accessResolver;

	private final Map<EntityReference, Map<String, Set<EntityReference>>> smartRefToSmartPopertyValues = newRefMap();
	private final Map<EntityReference, Map<String, Object>> smartRefToDelegatePopertyValues = newRefMap();

	public PropertyValueResolver(SmartManipulationProcessor manipulationProcessor, SmartAccess smartAccess, ModelExpert modelExpert,
			AccessResolver accessResolver) {

		this.manipulationProcessor = manipulationProcessor;
		this.smartAccess = smartAccess;
		this.modelExpert = modelExpert;
		this.accessResolver = accessResolver;
	}

	public void notifyChangeValue(EntityReference smartReference, String smartProperty, String delegateProperty, Object newSmartValue) {
		/* right now, we only care about the delegate properties if they are simple, and smart properties if they are
		 * entities/collections of entities */

		boolean simpleOrEnum = isSimpleOrEnum(newSmartValue);

		if (simpleOrEnum || newSmartValue == null) {
			acquireMap(smartRefToDelegatePopertyValues, smartReference).put(delegateProperty, newSmartValue);
		}

		if (!simpleOrEnum || newSmartValue == null) {
			Set<EntityReference> set = null;

			/* TODO REVIEW this is weird, we obviously also have Lists/Sets of simple types here, but it seems not to be
			 * a problem. Still, let's investigate how this should be done correctly. */
			if (newSmartValue instanceof EntityReference) {
				set = asSet((EntityReference) newSmartValue);

			} else if (newSmartValue instanceof List) {
				set = new HashSet<EntityReference>((List<EntityReference>) newSmartValue);

			} else if (newSmartValue instanceof Set) {
				set = (Set<EntityReference>) newSmartValue;
			}

			acquireMap(smartRefToSmartPopertyValues, smartReference).put(smartProperty, set);
		}
	}

	/**
	 * TODO also enum
	 */
	public Object acquireDelegatePropertyValue(EntityReference smartReference, String delegateProperty) {
		Map<String, Object> propertyMap = acquireMap(smartRefToDelegatePopertyValues, smartReference);

		Object result = propertyMap.get(delegateProperty);

		if (result == null) {
			result = retrieveDelegateValue(smartReference, delegateProperty);
			propertyMap.put(delegateProperty, result);
		}

		return result;
	}

	public Map<EntityReference, Object> acquireDelegatePropertyValues(Set<EntityReference> smartReferences, String delegateProperty) {
		Map<EntityReference, Object> result = newMap();
		List<EntityReference> unresolvedOwners = newList();

		// First we check the existing cache if some of the property values were already loaded before
		for (EntityReference smartReference : smartReferences) {
			Map<String, Object> propertyMap = acquireMap(smartRefToDelegatePopertyValues, smartReference);
			Object propertyValue = propertyMap.get(delegateProperty);

			if (propertyValue == null && !propertyMap.containsKey(delegateProperty)) {
				unresolvedOwners.add(smartReference);

			} else {
				result.put(smartReference, propertyValue);
			}
		}

		// Now we load all the property values which were not retrieved from cache in previous step
		Map<EntityReference, Object> newlyRetrievedValues = retrieveDelegateValues(unresolvedOwners, delegateProperty);
		for (Entry<EntityReference, Object> entry : newlyRetrievedValues.entrySet()) {
			acquireMap(smartRefToDelegatePopertyValues, entry.getKey()).put(delegateProperty, entry.getValue());
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	private Map<EntityReference, Object> retrieveDelegateValues(List<EntityReference> smartReferences, String delegateProperty) {
		Map<EntityReference, Object> result = newMap();

		/* TODO OPTIMIZE we could do this in one query (maybe, if common super-type is query-able, but that is a good
		 * assumption) */
		for (EntityReference ref : smartReferences) {
			result.put(ref, retrieveDelegateValue(ref, delegateProperty));
		}

		return result;
	}

	private Object retrieveDelegateValue(EntityReference smartReference, String delegateProperty) {
		if (!(smartReference instanceof PersistentEntityReference)) {
			throw new SmartQueryEvaluatorRuntimeException(
					"Cannot retrieve delegate property '" + delegateProperty + "' for preliminary smart entity: " + smartReference.getTypeSignature()
							+ "  because it was not set prior to doing this manipulation!");
		}

		com.braintribe.model.accessdeployment.IncrementalAccess accessDenotation = accessResolver.resolveAccess(smartReference);

		EntityMapping em = modelExpert.resolveEntityMapping(smartReference.getTypeSignature(), accessDenotation, USE_CASE);
		GmEntityType smartEntityType = em.getSmartEntityType();
		EntityPropertyMapping idEpm = modelExpert.resolveEntityPropertyMapping(smartEntityType, em.getAccess(), GenericEntity.id);

		String mappedIdPropertyName = idEpm.getDelegatePropertyName();
		Object delegateIdValue = manipulationProcessor.conv2Del(smartReference.getRefId(), idEpm.getConversion());

		if (mappedIdPropertyName.equals(delegateProperty)) {
			return delegateIdValue;
		}

		SelectQuery selectQuery = new SelectQueryBuilder().from(em.getDelegateEntityType().getTypeSignature(), "s").select()
				.property("s", delegateProperty).where().property("s", mappedIdPropertyName).eq().value(delegateIdValue).done();

		IncrementalAccess access = manipulationProcessor.getAccessImpl(em.getAccess());
		try {
			List<Object> results = access.query(selectQuery).getResults();

			switch (results.size()) {
				case 1:
					return results.get(0);
				case 0:
					throw new SmartAccessException(
							"No value found for delegte property '" + delegateProperty + "' of smart entity '" + toString(smartReference));
				default:
					throw new SmartAccessException("Somehow more than one value was found for delegate property '" + delegateProperty
							+ "' of smart entity '" + toString(smartReference));
			}

		} catch (ModelAccessException e) {
			throw new SmartAccessException("Error while evaluating query to resolve delegate property '" + delegateProperty + "' of smart entity: "
					+ smartReference.getTypeSignature(), e);
		}
	}

	private String toString(EntityReference ref) {
		return ref.getTypeSignature() + "[" + ref.getRefId() + "]";
	}

	public Set<EntityReference> acquireSmartPropertyValueReferences(EntityReference smartReference, String smartProperty, String joinIdProperty) {

		Map<String, Set<EntityReference>> propertyMap = acquireMap(smartRefToSmartPopertyValues, smartReference);

		if (propertyMap.containsKey(smartProperty)) {
			Set<EntityReference> result = propertyMap.get(smartProperty);

			return result != null ? result : Collections.<EntityReference> emptySet();
		}

		Set<EntityReference> result = retrieveSmartPropertyValueReferences(smartReference, smartProperty, joinIdProperty);
		propertyMap.put(smartProperty, result);

		return result;
	}

	private Set<EntityReference> retrieveSmartPropertyValueReferences(EntityReference smartReference, String smartProperty, String joinIdProperty) {

		if (!(smartReference instanceof PersistentEntityReference)) {
			throw new SmartQueryEvaluatorRuntimeException("Cannot retrieve delegate property '" + smartProperty + "' for preliminary smart entity: "
					+ smartReference.getTypeSignature() + "  because it was not set prior to doing this manipulation!");
		}

		PersistentEntityReference ref = (PersistentEntityReference) smartReference;

		// @formatter:off
		SelectQuery selectQuery = new SmartSelectQueryBuilder()
				.from(ref.getTypeSignature(), "s")
					.join("s", smartProperty, "p")
				.select().entitySignature().entity("p")
				.select("p", joinIdProperty)
				.select("p", GenericEntity.partition)
				.where()
					.entity("s").eq().entityReference(ref)
				.done();
		// @formatter:on

		try {
			List<Object> results = smartAccess.query(selectQuery).getResults();

			return convertTorefRences(results);

		} catch (ModelAccessException e) {
			throw new SmartAccessException(
					"Error while evaluating query to resolve smart property '" + smartProperty + "' of smart entity: " + ref.getTypeSignature(), e);
		}
	}

	private Set<EntityReference> convertTorefRences(List<Object> results) {
		Set<EntityReference> result = newSet();

		for (Object o : results) {
			ListRecord record = (ListRecord) o;

			PersistentEntityReference ref = PersistentEntityReference.T.create();
			ref.setTypeSignature((String) record.getValues().get(0));
			ref.setRefId(record.getValues().get(1));
			ref.setRefPartition((String) record.getValues().get(2));

			result.add(ref);
		}

		return result;
	}

	/**
	 * TODO explain we only need to cache values for simple/enum, but if we do for others, it is not error. This check
	 * just prevents (if the value is not null) unnecessary caching.
	 */
	private boolean isSimpleOrEnum(Object value) {
		if (value instanceof GenericEntity || value instanceof Collection || value instanceof Map) {
			return false;
		}

		return true;
	}

	public void onReferenceUpdate(EntityReference smartReference, EntityReference newSmartRef) {
		updateMapKey(smartRefToSmartPopertyValues, smartReference, newSmartRef);
		updateMapKey(smartRefToDelegatePopertyValues, smartReference, newSmartRef);
	}

}
