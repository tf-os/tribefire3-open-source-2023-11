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
package com.braintribe.model.processing.session.impl.managed.history;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Map;
import java.util.Set;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.core.commons.LocalEntityHashSupportWrapperCodec;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.Transaction;

@SuppressWarnings("unusable-by-js")
public class BasicTransaction extends AbstractTransactionFrame implements Transaction, ManipulationListener {
	private static class Counter {
		public int count;
	}

	private int hibernationDepth;
	private final Map<LocalEntityProperty, Counter> localManipulatedProperties = CodingMap
			.createHashMapBased(LocalEntityHashSupportWrapperCodec.INSTANCE);
	private final Set<GenericEntity> instantiatedEntities = newSet();
	private final Set<PersistentEntityReference> preliminarilyDeletedEntities = CodingSet.create(EntRefHashingComparator.INSTANCE);

	private AbstractTransactionFrame currentTransactionFrame = this;

	public BasicTransaction(ManagedGmSession session) {
		super(session);
	}

	public boolean wasPropertyManipulated(LocalEntityProperty entityProperty) {
		return localManipulatedProperties.containsKey(entityProperty);
	}

	@Override
	public boolean willPersist(GenericEntity entity) {
		return entity.session() == session && created(entity);
	}

	@Override
	public boolean created(GenericEntity entity) {
		return instantiatedEntities.contains(entity);
	}
	
	public boolean isPreliminarilyDeleted(EntityReference entityReference) {
		return preliminarilyDeletedEntities.contains(entityReference);
	}

	@Override
	public Set<LocalEntityProperty> getManipulatedProperties() {
		return localManipulatedProperties.keySet();
	}

	@Override
	public boolean hasManipulations() {
		return !doneManipulations.isEmpty();
	}

	@Override
	public void pushHibernation() {
		hibernationDepth++;
	}

	@Override
	public void popHibernation() {
		hibernationDepth--;
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if (hibernationDepth == 0) {
			if (session.getCompoundNotificationStack().isEmpty()) {
				currentTransactionFrame.appendManipulation(manipulation);
				onManipulationDone(manipulation);
			}
		}
	}

	@Override
	public AbstractTransactionFrame getCurrentTransactionFrame() {
		return currentTransactionFrame;
	}

	@Override
	public NestedTransaction beginNestedTransaction() {
		AbstractTransactionFrame transactionFrame = getCurrentTransactionFrame();
		BasicNestedTransaction nestedTransaction = new BasicNestedTransaction(session, this, transactionFrame);
		transactionFrame.setChildFrame(nestedTransaction);
		currentTransactionFrame = nestedTransaction;
		return nestedTransaction;
	}

	protected void endNestedTransaction() {
		if (currentTransactionFrame != this) {
			BasicNestedTransaction nestedTransaction = (BasicNestedTransaction) currentTransactionFrame;
			currentTransactionFrame = nestedTransaction.getParentFrame();
			currentTransactionFrame.setChildFrame(null);
		} else
			throw new IllegalStateException("no nested transaction present to be ended");
	}

	@Override
	protected void onManipulationUndone(Manipulation manipulation) {
		onManipulationDoneOrUndone(manipulation, false);
	}

	@Override
	protected void onManipulationDone(Manipulation manipulation) {
		onManipulationDoneOrUndone(manipulation, true);
	}

	private void onManipulationDoneOrUndone(Manipulation manipulation, boolean done) {
		if (manipulation instanceof PropertyManipulation) {
			onPropertyManipulationDoneOrUndone((PropertyManipulation) manipulation, done);
			return;
		}

		switch (manipulation.manipulationType()) {
			case COMPOUND:
				onCompoundManipulationDoneOrUndone((CompoundManipulation) manipulation, done);
				return;
			case INSTANTIATION:
				onInstantiationManipulationDoneOrUndone((InstantiationManipulation) manipulation, done);
				return;
			case DELETE:
				onDeleteManipulationDoneOrUndone((DeleteManipulation) manipulation, done);
				return;
			default:
				return;
		}
	}

	private void onPropertyManipulationDoneOrUndone(PropertyManipulation pm, boolean done) {
		LocalEntityProperty entityProperty = (LocalEntityProperty) pm.getOwner();
		if (done)
			addTouchCount(entityProperty, localManipulatedProperties);
		else
			removeTouchCount(entityProperty, localManipulatedProperties);
	}

	private void onCompoundManipulationDoneOrUndone(CompoundManipulation cm, boolean done) {
		for (Manipulation nestedManipulation : cm.getCompoundManipulationList())
			onManipulationDoneOrUndone(nestedManipulation, done);
	}

	private void onInstantiationManipulationDoneOrUndone(InstantiationManipulation im, boolean done) {
		GenericEntity ge = im.getEntity();
		if (done)
			instantiatedEntities.add(ge);
	}

	private void onDeleteManipulationDoneOrUndone(DeleteManipulation dm, boolean done) {
		GenericEntity entity = dm.getEntity();
		if (entity.getId() == null)
			return;

		EntityReference reference = entity.reference();

		if (done)
			preliminarilyDeletedEntities.add((PersistentEntityReference) reference);
		else
			preliminarilyDeletedEntities.remove(reference);
	}

	protected <T extends Owner> void addTouchCount(T entityProperty, Map<T, Counter> manipulatedProperties) {
		Counter counter = manipulatedProperties.computeIfAbsent(entityProperty, ep -> new Counter());
		counter.count++;
	}

	protected <T extends Owner> void removeTouchCount(T entityProperty, Map<T, Counter> manipulatedProperties) {
		manipulatedProperties.compute(entityProperty, (ep, counter) -> {
			return (--counter.count == 0) ? null : counter;
		});
	}

	@Override
	public void clear() {
		localManipulatedProperties.clear();
		instantiatedEntities.clear();
		preliminarilyDeletedEntities.clear();
		super.clear();
	}
}
