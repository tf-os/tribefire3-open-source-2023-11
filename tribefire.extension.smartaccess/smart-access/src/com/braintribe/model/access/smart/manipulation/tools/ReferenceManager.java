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
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.createUpdatedReference;

import java.util.Map;

import com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.smart.SmartAccessException;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;

/**
 * This class keeps track of mapping between smart-level references and delegate-level references.
 * 
 * @see #acquireDelegateReference(EntityReference)
 * @see #acquireDelegateReference(EntityReference, EntityMapping)
 * @see #acquireSmartReference(EntityReference, EntityMapping)
 */
public class ReferenceManager {

	private final SmartManipulationProcessor manipulationProcessor;
	private final ModelExpert modelExpert;
	private final AccessResolver accessResolver;

	private final Map<EntityReference, EntityReference> smartToDelegateRefs = newRefMap();
	private final Map<EntityReference, EntityReference> delegateToSmartRefs = newRefMap();

	public ReferenceManager(SmartManipulationProcessor manipulationProcessor, ModelExpert modelExpert, AccessResolver accessResolver) {
		this.modelExpert = modelExpert;
		this.manipulationProcessor = manipulationProcessor;
		this.accessResolver = accessResolver;
	}

	/**
	 * Same as {@link #acquireDelegateReference(EntityReference, EntityMapping)}, just computes the required {@link EntityMapping} first.
	 */
	public <T extends EntityReference> T acquireDelegateReference(T smartReference) {
		IncrementalAccess access = manipulationProcessor.accessResolver().resolveAccess(smartReference);
		EntityMapping em = modelExpert.resolveEntityMapping(smartReference.getTypeSignature(), access, USE_CASE);
		return acquireDelegateReference(smartReference, em);
	}

	/**
	 * @return delegate-level {@link EntityReference} corresponding to given smart-level reference. Invoking this method multiple times with
	 *         "equivalent" references always returns the very same instance
	 */
	public <T extends EntityReference> T acquireDelegateReference(T smartReference, EntityMapping em) {
		EntityReference ref = smartToDelegateRefs.get(smartReference);

		if (ref == null) {
			if (smartReference instanceof PreliminaryEntityReference) {
				ref = PreliminaryEntityReference.T.create();
				ref.setRefId(smartReference.getRefId());

			} else {
				ref = PersistentEntityReference.T.create();
				ref.setRefId(toDelegateId(smartReference));
			}

			ref.setTypeSignature(em.getDelegateEntityType().getTypeSignature());
			ref.setRefPartition(smartReference.getRefPartition());

			smartToDelegateRefs.put(smartReference, ref);
			delegateToSmartRefs.put(ref, smartReference);
		}

		return (T) ref;
	}

	private Object toDelegateId(EntityReference smartReference) {
		return manipulationProcessor.propertyValueResolver().acquireDelegatePropertyValue(smartReference, GenericEntity.id);
	}

	/**
	 * @return smart reference associated with given delegate reference
	 * @throws SmartAccessException
	 *             if given reference is a {@link PreliminaryEntityReference} which was not created by this class (using some of the
	 *             acquireDelegateReference methods) by converting a smart {@linkplain PreliminaryEntityReference}. This is not expected, cause such
	 *             reference can never resolved.
	 */
	public <T extends EntityReference> T acquireSmartReference(T delegateReference, EntityMapping em) {
		EntityReference ref = delegateToSmartRefs.get(delegateReference);

		if (ref == null) {
			if (delegateReference instanceof PreliminaryEntityReference) {
				throw new SmartAccessException("Preliminary reference not expected here. Seems like some delegate returned an induced manipulation"
						+ " containing a PreliminaryReference which was not part of the manipulation request." + " Referenced type: "
						+ delegateReference.getTypeSignature());
			}

			ref = PersistentEntityReference.T.create();
			ref.setTypeSignature(em.getSmartEntityType().getTypeSignature());
			ref.setRefId(toSmartId(delegateReference.getRefId(), em));
			ref.setRefPartition(delegateReference.getRefPartition());

			delegateToSmartRefs.put(delegateReference, ref);
		}

		return (T) ref;
	}

	private Object toSmartId(Object id, EntityMapping em) {
		return manipulationProcessor.conv2Smart(id, getIdEpm(em).getConversion(), null /* EntityMapping only needed for refs */, false);
	}

	/**
	 * @return {@link EntityPropertyMapping} corresponding to the id property of a smart entity represented by it's {@link EntityMapping}.
	 */
	private EntityPropertyMapping getIdEpm(EntityMapping em) {
		return modelExpert.resolveEntityPropertyMapping(em.getSmartEntityType(), em.getAccess(), GenericEntity.id);
	}

	public void notifyChangeValueForUnmapped(EntityReference dgReference, GmProperty dgProperty, Object newDgValue) {
		if (!dgProperty.isId())
			return;

		EntityReference smartReference = delegateToSmartRefs.get(dgReference);
		if (smartReference == null) {
			/* This means we have a induced CVM manipulation on an unmapped id property of a delegate entity, where the corresponding smart entity was
			 * not part of the request (sent from smart access to delegates), so there is no reference to update and we can thus ignore this
			 * manipulation. If there is some other induced manipulation for this entity, it will be handled the right way, cause that's the same as
			 * if we had some induced manipulation to a not-yet-encountered persistent delagate reference. */
			return;
		}

		EntityReference newDelegateRef = createUpdatedReference(dgReference, dgProperty.getName(), newDgValue);
		updateReferenceMapping(smartReference, smartReference, newDelegateRef);
	}

	public void notifyChangeValue(EntityReference smartReference, String smartProperty, Object newSmartValue, EntityReference dgReference,
			String dgPropertyName, boolean isDgId, Object newDgValue) {

		if (!GenericEntity.id.equals(smartProperty) && !GenericEntity.partition.equals(smartProperty))
			return;

		if (newSmartValue == null && GenericEntity.id.equals(smartProperty)) {
			if (smartReference.referenceType() == EntityReferenceType.preliminary)
				return;

			throw new SmartAccessException("Cannot set id to null for an entity that already has an id. Manipulation owner: " + smartReference);
		}

		EntityReference newSmartRef = createUpdatedReference(smartReference, smartProperty, newSmartValue);
		EntityReference newDelegateRef = newDelegateRef(smartReference, dgReference, dgPropertyName, isDgId, newDgValue);

		updateReferenceMapping(smartReference, newSmartRef, newDelegateRef);

		accessResolver.onReferenceUpdate(smartReference, newSmartRef);
		manipulationProcessor.propertyValueResolver().onReferenceUpdate(smartReference, newSmartRef);
	}

	private EntityReference newDelegateRef(EntityReference smRef, EntityReference dgRef, String dgName, boolean isDgId, Object newDgValue) {
		return isDgId ? createUpdatedReference(dgRef, dgName, newDgValue) : smartToDelegateRefs.get(smRef);
	}

	private void updateReferenceMapping(EntityReference smartReference, EntityReference newSmartRef, EntityReference newDelegateRef) {
		EntityReference delegateRef = smartToDelegateRefs.remove(smartReference);
		smartToDelegateRefs.put(newSmartRef, newDelegateRef);

		delegateToSmartRefs.remove(delegateRef);
		delegateToSmartRefs.put(newDelegateRef, newSmartRef);
	}

}
