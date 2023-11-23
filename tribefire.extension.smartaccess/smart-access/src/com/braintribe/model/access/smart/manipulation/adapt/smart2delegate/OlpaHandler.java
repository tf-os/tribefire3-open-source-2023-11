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
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smart.manipulation.SmartManipulationContextVariables;
import com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.OrderedLinkPropertyAssignment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.manipulator.expert.basic.collection.ListManipulator;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.record.ListRecord;

/**
 * Covers properties with {@link OrderedLinkPropertyAssignment} mapping.
 * 
 * For implementation overview see {@link #convertToDelegate(ChangeValueManipulation)}.
 */
public class OlpaHandler implements Smart2DelegateHandler<OrderedLinkPropertyAssignment> {

	private final SmartManipulationProcessor smp;
	private final SmartManipulationContextVariables $;

	private OrderedLinkPropertyAssignment currentOlpa;
	private String linkEntitySignature;
	private String linkOtherKey;
	private Object keyValue;
	private List<Manipulation> linkAccessManipulations;
	private IncrementalAccess linkAccess;

	private static final boolean ADD = true;
	private static final boolean REMOVE = !ADD;

	/** Maps linked references (list elements) to their key-property values. */
	private Map<EntityReference, Object> referenceMap;
	/** List representing to the existing list property */
	private List<LinkEntry> existingLinkedEntries;
	/** List which will represent the new list property - elements are key-property values. */
	private final List<Object> newList = newList();

	ListManipulator listManipulator = ListManipulator.INSTANCE;

	public OlpaHandler(SmartManipulationProcessor smp) {
		this.smp = smp;
		this.$ = smp.context();
	}

	/* This is used as local variable inside methods, but is declared here to make code nicer */
	protected Manipulation delegateManipulation;

	@Override
	public void loadAssignment(OrderedLinkPropertyAssignment assignment) {
		currentOlpa = assignment;

		linkEntitySignature = currentOlpa.getLinkKey().getDeclaringType().getTypeSignature();
		linkOtherKey = currentOlpa.getLinkOtherKey().getName();

		keyValue = smp.propertyValueResolver().acquireDelegatePropertyValue($.currentSmartReference, currentOlpa.getKey().getProperty().getName());

		linkAccess = currentOlpa.getLinkAccess();
		linkAccessManipulations = acquireList($.delegateManipulations, linkAccess);

		newList.clear();
	}

	/**
	 * The general flow for every manipulation is as follows:
	 * 
	 * <ol>
	 * <li>{@link #loadReferenceMap(Collection)} - prepare {@link #referenceMap}</li>
	 * <li>{@link #loadExistingLinkEntires(Object)} - prepare {@link #existingLinkedEntries}</li>
	 * <li>prepare the {@link #newList}</li>
	 * <li>{@link #applyDiff()} invocation</li>
	 * </ol>
	 */
	@Override
	public void convertToDelegate(ChangeValueManipulation manipulation) throws ModelAccessException {
		List<?> newValue = (List<?>) manipulation.getNewValue();

		loadReferenceMap(newValue);
		loadExistingLinkEntires(keyValue);

		for (Object ref : newValue) {
			newList.add(keyForLinkedReference((EntityReference) ref));
		}

		applyDiff();
	}

	/** @see #convertToDelegate(ChangeValueManipulation) */
	@Override
	public void convertToDelegate(AddManipulation manipulation) throws ModelAccessException {
		processAddOrRemove(manipulation.getItemsToAdd(), ADD);
	}

	/** @see #convertToDelegate(ChangeValueManipulation) */
	@Override
	public void convertToDelegate(RemoveManipulation manipulation) throws ModelAccessException {
		processAddOrRemove(manipulation.getItemsToRemove(), REMOVE);
	}

	private void processAddOrRemove(Map<?, ?> manipulationMap, boolean add) throws ModelAccessException {
		loadReferenceMap(manipulationMap.values());
		loadExistingLinkEntires(keyValue);

		for (LinkEntry e : existingLinkedEntries) {
			newList.add(e.otherKeyValue);
		}

		if (add) {
			listManipulator.insert(newList, convertManipulationMap(manipulationMap));
		} else {
			listManipulator.remove(newList, convertManipulationMap(manipulationMap));
		}

		applyDiff();
	}

	/** @see #convertToDelegate(ChangeValueManipulation) */
	@Override
	public void convertToDelegate(ClearCollectionManipulation manipulation) throws ModelAccessException {
		loadExistingLinkEntires(keyValue);

		applyDiff();
	}

	/**
	 * First computes the difference from previous state of the list (i.e. creates an instance of {@link Delta}) based
	 * on {@link #existingLinkedEntries} and {@link #newList}, and then creates right manipulations for all three types
	 * of changes - adds/removes/propertyUpdates.
	 */
	private void applyDiff() {
		Delta delta = computeDelta();

		for (Entry<LinkEntry, Object> e : delta.changeEntries.entrySet()) {
			EntityReference ref = persistentRef(linkEntitySignature, e.getKey().linkId);
			linkAccessManipulations.add(changeValue(owner(ref, linkOtherKey), e.getValue()));
		}

		for (LinkEntry e : delta.newEntries) {
			EntityReference ref = preliminaryRef(linkEntitySignature);

			linkAccessManipulations.add(instantiationManipulation(ref));
			linkAccessManipulations.add(changeValue(owner(ref, currentOlpa.getLinkKey()), keyValue));
			linkAccessManipulations.add(changeValue(owner(ref, currentOlpa.getLinkOtherKey()), e.otherKeyValue));
			linkAccessManipulations.add(changeValue(owner(ref, currentOlpa.getLinkIndex()), e.index));
		}

		for (LinkEntry e : delta.removedEntries) {
			PersistentEntityReference ref = persistentRef(linkEntitySignature, e.linkId);
			linkAccessManipulations.add(delete(ref, DeleteMode.ignoreReferences));
		}
	}

	private Delta computeDelta() {
		Delta result = new Delta();

		int size = Math.min(existingLinkedEntries.size(), newList.size());
		for (int i = 0; i < size; i++) {
			LinkEntry existingEntry = existingLinkedEntries.get(i);
			Object newEntryValue = newList.get(i);

			if (!existingEntry.otherKeyValue.equals(newEntryValue)) {
				result.changeEntries.put(existingEntry, newEntryValue);
			}
		}

		/* adding new elements - this will only do something iff newList's size is bigger than existingLinkedEntries */
		for (int i = size; i < existingLinkedEntries.size(); i++) {
			result.removedEntries.add(existingLinkedEntries.get(i));
		}

		/* removing elements - this will only do something iff existingLinkedEntries's size is bigger than newList */
		for (int i = size; i < newList.size(); i++) {
			result.newEntries.add(new LinkEntry(null, newList.get(i), i));
		}

		return result;
	}

	private void loadReferenceMap(Collection<?> otherReferences) {
		final String otherKey = currentOlpa.getOtherKey().getProperty().getName();

		@SuppressWarnings("unchecked")
		HashSet<EntityReference> otherReferencesSet = new HashSet<EntityReference>((Collection<EntityReference>) otherReferences);
		referenceMap = smp.propertyValueResolver().acquireDelegatePropertyValues(otherReferencesSet, otherKey);
	}

	/**
	 * Load the list {@link #existingLinkedEntries} with all entries representing elements of the list for given
	 * {@link #keyValue} (i.e. current owner). The list is also sorted in the right order.
	 */
	private void loadExistingLinkEntires(Object linkKeyValue) throws ModelAccessException {
		final String linkOtherKey = currentOlpa.getLinkOtherKey().getName();
		final String linkKey = currentOlpa.getLinkKey().getName();
		final String linkIndex = currentOlpa.getLinkIndex().getName();

		// @formatter:off
		SelectQuery query = new SelectQueryBuilder()
						.from(linkEntitySignature, "l")
						.select("l", GenericEntity.id)
						.select("l", linkOtherKey)
						.where()
							.property("l", linkKey).eq(linkKeyValue)
						.orderBy()
							.property("l", linkIndex)
				.done();
		// @formatter:on

		existingLinkedEntries = newList();

		int index = 0;
		SelectQueryResult qResult = smp.getAccessImpl(linkAccess).query(query);
		for (Object _row : qResult.getResults()) {
			ListRecord row = (ListRecord) _row;
			List<Object> values = row.getValues();

			existingLinkedEntries.add(new LinkEntry(values.get(0), values.get(1), index++));
		}
	}

	/**
	 * The map given as parameter comes from the smart manipulation, i.e. maps indices (integers) to smart references,
	 * the map on the output maps the same indices to the corresponding key property values. This conversion is based on
	 * {@link #referenceMap} computed in {@link #loadReferenceMap(Collection)}. Also, cast is made for convenience.
	 */
	private Map<Integer, Object> convertManipulationMap(Map<?, ?> itemsToAdd) {
		Map<Integer, Object> result = newMap();

		for (Map.Entry<?, ?> e : itemsToAdd.entrySet()) {
			result.put((Integer) e.getKey(), keyForLinkedReference((EntityReference) e.getValue()));
		}

		return result;
	}

	private Object keyForLinkedReference(EntityReference value) {
		return referenceMap.get(value);
	}

	private static class LinkEntry {
		Object linkId;
		Object otherKeyValue;
		int index;

		public LinkEntry(Object linkId, Object otherKeyValue, int index) {
			this.linkId = linkId;
			this.otherKeyValue = otherKeyValue;
			this.index = index;
		}
	}

	private static class Delta {
		Map<LinkEntry, Object> changeEntries = newMap();
		Collection<LinkEntry> newEntries = newList();
		Collection<LinkEntry> removedEntries = newList();
	}

}
