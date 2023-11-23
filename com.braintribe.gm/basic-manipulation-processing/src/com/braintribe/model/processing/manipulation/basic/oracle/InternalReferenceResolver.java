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
package com.braintribe.model.processing.manipulation.basic.oracle;

import static com.braintribe.model.generic.manipulation.ManipulationType.CHANGE_VALUE;
import static com.braintribe.model.generic.manipulation.ManipulationType.INSTANTIATION;

import java.util.Map;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.builder.vd.VdBuilder;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;

/**
 * @author peter.gazdik
 */
/* package */ class InternalReferenceResolver {

	/**
	 * Returns a {@link CodingMap} backed by a {@link EntRefHashingComparator}.
	 */
	public static Map<EntityReference, PersistentEntityReference> resolve(Manipulation manipulation, Manipulation inducedManipulation) {
		return new InternalReferenceResolver(manipulation, inducedManipulation).resolve();
	}

	private final Map<EntityReference, EntityReference> previousRef = CodingMap.create(EntRefHashingComparator.INSTANCE);
	private final Map<EntityReference, EntityReference> result = CodingMap.create(EntRefHashingComparator.INSTANCE);

	private final Manipulation manipulation;
	private final Manipulation inducedManipulation;

	public InternalReferenceResolver(Manipulation manipulation, Manipulation inducedManipulation) {
		this.manipulation = manipulation;
		this.inducedManipulation = inducedManipulation;
	}

	private Map<EntityReference, PersistentEntityReference> resolve() {
		process(manipulation);
		process(inducedManipulation);

		validateResultContainsOnlyPersistentReferences();

		return (Map<EntityReference, PersistentEntityReference>) (Map<?, ?>) result;
	}

	private void process(Manipulation m) {
		m.stream().forEach(this::onAtomic);
	}

	private void onAtomic(AtomicManipulation am) {
		if (am.manipulationType() == INSTANTIATION)
			onInstantiation((InstantiationManipulation) am);

		else if (am.manipulationType() == CHANGE_VALUE)
			onChangeValue((ChangeValueManipulation) am);
	}

	private void onInstantiation(InstantiationManipulation im) {
		PreliminaryEntityReference prelimRef = (PreliminaryEntityReference) im.getEntity();
		previousRef.put(prelimRef, prelimRef);
	}

	private void onChangeValue(ChangeValueManipulation am) {
		EntityProperty entityProperty = (EntityProperty) am.getOwner();
		EntityReference ref = entityProperty.getReference();

		if (GenericEntity.id.equals(entityProperty.getPropertyName())) {
			Object newId = am.getNewValue();
			if (isIdAssignmentOk(ref, newId))
				changeRef(ref, PersistentEntityReference.T, newId, ref.getRefPartition());

		} else if (GenericEntity.partition.equals(entityProperty.getPropertyName()))
			changeRef(ref, ref.entityType(), ref.getRefId(), (String) am.getNewValue());
	}

	private boolean isIdAssignmentOk(EntityReference ref, Object newId) {
		if (newId != null)
			return true;

		if (ref.referenceType() == EntityReferenceType.preliminary)
			return false; // ignore null assignment to preliminary entity

		throw new IllegalArgumentException("Cannot assign null value to id property of a persisted entity: " + ref);
	}

	private void changeRef(EntityReference ref, EntityType<? extends EntityReference> refType, Object newId, String newPartition) {
		EntityReference newRef = VdBuilder.reference(refType, ref.getTypeSignature(), newId, newPartition);

		previousRef.put(newRef, ref);

		while (true) {
			result.put(ref, newRef);

			EntityReference prevRef = getPreviousRef(ref);
			if (EntRefHashingComparator.INSTANCE.compare(prevRef, ref))
				return;

			ref = prevRef;
		}
	}

	private EntityReference getPreviousRef(EntityReference ref) {
		return previousRef.computeIfAbsent(ref, this::visitUnknownRef);
	}

	private EntityReference visitUnknownRef(EntityReference ref) {
		if (ref.referenceType() != EntityReferenceType.preliminary)
			/* This might mean an already existing entity has id or partition changed. We're not gonna stop that here,
			 * just play along */
			return ref;

		// There is a preliminary reference which didn't have an instantiation first - we say it is an error
		throw new IllegalStateException("Preliminary reference encountered, but corresponding Instantiation is missing. Reference: " + ref);
	}

	private void validateResultContainsOnlyPersistentReferences() {
		for (EntityReference ref : result.values())
			if (ref.referenceType() != EntityReferenceType.persistent)
				throw new IllegalStateException("Cannot resolve preliminary reference to a persistent one: " + ref);
	}

}
