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
package com.braintribe.model.access.security.manipulation.experts;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityContext;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpert;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpositionContext;
import com.braintribe.model.processing.security.manipulation.SecurityViolationEntry;

/**
 * {@link ManipulationSecurityExpert} for {@link Mandatory} constraint.
 */
public class MandatoryPropertyExpert implements ManipulationSecurityExpert {

	@Override
	public Object createExpertContext(ManipulationSecurityContext context) {
		return newMap();
	}

	private static class EntityEntry {
		EntityReference ref;
		Map<String, AtomicManipulation> propertiesSetToNull = newMap();
	}

	@Override
	public void expose(ManipulationSecurityExpositionContext context) {
		if (context.getTargetReference() == null) {
			return;
		}

		switch (context.getCurrentManipulationType()) {
			case CHANGE_VALUE:
				notifyNewValue(context);
				checkIfIdChange(context);
				return;

			case DELETE:
				Map<EntityReference, EntityEntry> map = context.getExpertContext();
				map.remove(context.getNormalizedTargetReference());
				return;

			case INSTANTIATION:
				notifyInstantiation(context);
				return;

			default:
				return;
		}
	}

	private void notifyNewValue(ManipulationSecurityExpositionContext context) {
		ChangeValueManipulation cvm = (ChangeValueManipulation) context.getCurrentManipulation();
		Object newValue = cvm.getNewValue();
		EntityEntry entry = acquireEntityEntry(context);

		String propertyName = context.getTargetPropertyName();

		if (newValue == null) {
			entry.propertiesSetToNull.put(propertyName, cvm);
		} else {
			entry.propertiesSetToNull.remove(propertyName);
		}
	}

	private void checkIfIdChange(ManipulationSecurityExpositionContext context) {
		ManipulationSecurityExpertTools.updateIdIfNeededChange(context, context.<Map<?, ?>> getExpertContext());
	}

	private void notifyInstantiation(ManipulationSecurityExpositionContext context) {
		EntityEntry entry = acquireEntityEntry(context);

		EntityType<?> entityType = context.getTargetEntityType();
		for (Property p : entityType.getProperties()) {
			entry.propertiesSetToNull.put(p.getName(), context.getCurrentManipulation());
		}
	}

	private EntityEntry acquireEntityEntry(ManipulationSecurityExpositionContext context) {
		EntityReference reference = context.getNormalizedTargetReference();

		Map<EntityReference, EntityEntry> map = context.getExpertContext();
		EntityEntry entry = map.get(reference);

		if (entry == null) {
			entry = new EntityEntry();
			entry.ref = context.getTargetReference();
			map.put(reference, entry);
		}

		return entry;
	}

	@Override
	public void validate(ManipulationSecurityContext context) {
		Map<GenericEntity, EntityEntry> map = context.getExpertContext();

		for (EntityEntry entityEntry : map.values())
			validate(entityEntry, context);
	}

	private void validate(EntityEntry entry, ManipulationSecurityContext context) {
		if (entry.propertiesSetToNull.isEmpty())
			return;

		String typeSignature = entry.ref.getTypeSignature();
		// Being here means the Instance is still there and we set some of the properties as null.

		for (Entry<String, AtomicManipulation> nullPropertyEntry : entry.propertiesSetToNull.entrySet()) {
			String propertyName = nullPropertyEntry.getKey();
			AtomicManipulation causingManipulation = nullPropertyEntry.getValue();

			if (isPropertyMandatory(typeSignature, propertyName, context)) {
				SecurityViolationEntry validationEntry = SecurityViolationEntry.T.create();
				validationEntry.setEntityReference(entry.ref);
				validationEntry.setPropertyName(propertyName);
				validationEntry.setCausingManipulation(causingManipulation);
				validationEntry.setDescription("[MandatoryProperty] Property: " + typeSignature + "." + propertyName);

				context.addViolationEntry(validationEntry);
			}
		}
	}

	/** This may be overridden by sub-class if other method should be used to find out if property is mandatory. */
	protected boolean isPropertyMandatory(String typeSignature, String propertyName, ManipulationSecurityContext context) {
		return context.getCmdResolver().getMetaData() //
				.entityTypeSignature(typeSignature) //
				.property(propertyName) //
				.is(Mandatory.T);

	}

}
