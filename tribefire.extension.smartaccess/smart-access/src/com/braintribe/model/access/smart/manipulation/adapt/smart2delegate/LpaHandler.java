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
package com.braintribe.model.access.smart.manipulation.adapt.smart2delegate;

import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.changeValue;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.delete;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.instantiationManipulation;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.owner;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.persistentRef;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.preliminaryRef;
import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smart.manipulation.SmartManipulationContextVariables;
import com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.LinkPropertyAssignment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.record.ListRecord;

/**
 * Covers properties with {@link LinkPropertyAssignment} mapping.
 */
public class LpaHandler implements Smart2DelegateHandler<LinkPropertyAssignment> {

	private final SmartManipulationProcessor smp;
	private final SmartManipulationContextVariables $;

	private LinkPropertyAssignment currentLpa;
	private String linkEntitySignature;
	private Object keyValue;
	private List<Manipulation> linkAccessManipulations;
	private IncrementalAccess linkAccess;

	public LpaHandler(SmartManipulationProcessor smp) {
		this.smp = smp;
		this.$ = smp.context();
	}

	/* This is used as local variable inside methods, but is declared here to make code nicer */
	protected Manipulation delegateManipulation;

	@Override
	public void loadAssignment(LinkPropertyAssignment assignment) {
		currentLpa = assignment;

		linkEntitySignature = currentLpa.getLinkKey().getDeclaringType().getTypeSignature();

		keyValue = smp.propertyValueResolver().acquireDelegatePropertyValue($.currentSmartReference, currentLpa.getKey().getProperty().getName());

		linkAccess = currentLpa.getLinkAccess();
		linkAccessManipulations = acquireList($.delegateManipulations, linkAccess);
	}

	@Override
	public void convertToDelegate(ChangeValueManipulation manipulation) throws ModelAccessException {
		Collection<Object> otherKeyValues = findOtherKeyValues((Set<?>) manipulation.getNewValue());
		Map<Object, Object> existingLinkedKeyValues = findLinkOtherKeysFor(keyValue);

		addNewLinkEntities(otherKeyValues, existingLinkedKeyValues);
		removeExistingLinkEntities(existingLinkedKeyValues);
	}

	@Override
	public void convertToDelegate(AddManipulation manipulation) throws ModelAccessException {
		Collection<Object> otherKeyValues = findOtherKeyValues(manipulation.getItemsToAdd().keySet());
		Map<Object, Object> existingLinkedKeyValues = findLinkOtherKeysFor(keyValue);

		addNewLinkEntities(otherKeyValues, existingLinkedKeyValues);
	}

	@Override
	public void convertToDelegate(RemoveManipulation manipulation) throws ModelAccessException {
		Collection<Object> otherKeyValues = findOtherKeyValues(manipulation.getItemsToRemove().keySet());
		Map<Object, Object> existingLinkedKeyValues = findLinkOtherKeysFor(keyValue);
		existingLinkedKeyValues.keySet().retainAll(otherKeyValues);

		removeExistingLinkEntities(existingLinkedKeyValues);
	}

	@Override
	public void convertToDelegate(ClearCollectionManipulation manipulation) throws ModelAccessException {
		Map<Object, Object> existingLinkedKeyValues = findLinkOtherKeysFor(keyValue);

		removeExistingLinkEntities(existingLinkedKeyValues);
	}

	private void addNewLinkEntities(Collection<Object> othersToAdd, Map<Object, Object> existingLinkedKeyValues) {
		for (Object otherKeyValue : othersToAdd) {
			if (existingLinkedKeyValues.containsKey(otherKeyValue)) {
				existingLinkedKeyValues.remove(otherKeyValue);
				continue;
			}

			PreliminaryEntityReference ref = preliminaryRef(linkEntitySignature);

			linkAccessManipulations.add(instantiationManipulation(ref));
			linkAccessManipulations.add(changeValue(owner(ref, currentLpa.getLinkKey()), keyValue));
			linkAccessManipulations.add(changeValue(owner(ref, currentLpa.getLinkOtherKey()), otherKeyValue));
		}
	}

	private void removeExistingLinkEntities(Map<Object, Object> existingLinkedKeyValues) {
		for (Object linkIdValue : existingLinkedKeyValues.values()) {
			PersistentEntityReference ref = persistentRef(linkEntitySignature, linkIdValue);
			linkAccessManipulations.add(delete(ref, DeleteMode.ignoreReferences));
		}
	}

	/**
	 * @return map where key is a reference given as parameter and value is corresponding property which is used as key
	 *         for the link
	 */
	@SuppressWarnings("unchecked")
	private Collection<Object> findOtherKeyValues(Set<?> otherReferences) {
		if (otherReferences.isEmpty()) {
			return Collections.emptySet();
		}

		Set<EntityReference> castedReferences = (Set<EntityReference>) otherReferences;

		String otherKey = currentLpa.getOtherKey().getProperty().getName();
		EntityReference ref = first(castedReferences);
		otherKey = smp.findDelegatePropertyForKeyPropertyOfCurrentSmartType(otherKey, ref);

		return smp.propertyValueResolver().acquireDelegatePropertyValues(castedReferences, otherKey).values();
	}

	/**
	 * @return map where key is a <tt>linkOtherKey</tt> and value is a corresponding LinkEntity <tt>id</tt>.
	 */
	private Map<Object, Object> findLinkOtherKeysFor(Object linkKeyValue) throws ModelAccessException {
		final String linkOtherKey = currentLpa.getLinkOtherKey().getName();
		final String linkKey = currentLpa.getLinkKey().getName();

		// @formatter:off
		SelectQuery query = new SelectQueryBuilder()
						.from(linkEntitySignature, "l")
						.select("l", linkOtherKey)
						.select("l", GenericEntity.id)
						.where()
							.property("l", linkKey).eq(linkKeyValue)
				.done();
		// @formatter:on

		Map<Object, Object> result = new HashMap<Object, Object>();

		SelectQueryResult qResult = smp.getAccessImpl(linkAccess).query(query);
		for (Object _row : qResult.getResults()) {
			ListRecord row = (ListRecord) _row;
			List<Object> values = row.getValues();

			result.put(values.get(0), values.get(1));
		}

		return result;
	}

}
