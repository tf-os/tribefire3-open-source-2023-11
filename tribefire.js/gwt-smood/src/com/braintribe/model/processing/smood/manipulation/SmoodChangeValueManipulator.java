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
package com.braintribe.model.processing.smood.manipulation;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.ManipulationTrackingPropertyAccessInterceptor;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.manipulator.api.Manipulator;
import com.braintribe.model.processing.manipulator.api.ManipulatorContext;

/**
 * @author peter.gazdik
 */
public class SmoodChangeValueManipulator implements Manipulator<ChangeValueManipulation> {

	private final boolean ignorePartitions;
	private final boolean useGlobalIdAsId;

	public SmoodChangeValueManipulator(boolean ignorePartitions, boolean useGlobalIdAsId) {
		this.ignorePartitions = ignorePartitions;
		this.useGlobalIdAsId = useGlobalIdAsId;
	}

	@Override
	public void apply(ChangeValueManipulation manipulation, ManipulatorContext context) {
		LocalEntityProperty owner = context.resolveOwner(manipulation);

		String propertyName = owner.getPropertyName();

		if (ignorePartitions && GenericEntity.partition.equals(propertyName))
			return;

		GenericEntity entity = owner.getEntity();
		Object newValue = manipulation.getNewValue();

		if (useGlobalIdAsId && GenericEntity.globalId.equals(propertyName) && !canAssignGlobalId(entity, newValue))
			throw new IllegalArgumentException(
					"Cannot change global id of an entity: " + entity + ". Current value: " + entity.getGlobalId() + ", new value: " + newValue);

		Property property = entity.entityType().getProperty(propertyName);
		assignPropertySafely(context, entity, newValue, property);
	}

	private boolean canAssignGlobalId(GenericEntity entity, Object newValue) {
		return entity.getGlobalId() == null || entity.getGlobalId().equals(newValue);
	}

	/**
	 * This method ensures the application of this manipulation has some transactional qualities .<br>
	 * 
	 * Smood updates it's indices based on {@link ChangeValueManipulation}s is that it listens to the changes done by
	 * this manipulator. Now, if an error happens while processing this notification, we might end up in an invalid
	 * state, because the index was not updated, but the property of the entity was already changed (because the
	 * {@link ManipulationTrackingPropertyAccessInterceptor}) first applies the change, then notifies listeners). This
	 * method therefore fixes it by setting back the original value.<br>
	 * 
	 * For this reason, the Smood's listener registers itself as the first listener in the chain, and for this to work
	 * properly, no listener on Smood's session ever should register itself as first. Otherwise it's gonna be notified
	 * with changes that might be undone silently.<br>
	 * 
	 * This method is especially important if id property of a new entity is assigned to an already used id. If we
	 * didn't set the property back, the subsequent undoing of the already-applied manipulations would not work. An
	 * attempt to undo the instantiation, thus removing the entity from the population manager, would fail, as the
	 * entity is indexed with id being <tt>null</tt>, but the entity that is being removed from population manager has
	 * id assigned.
	 */
	private void assignPropertySafely(ManipulatorContext context, GenericEntity entity, Object newValue, Property property) {
		Object originalValue = property.getDirectUnsafe(entity);

		try {
			property.set(entity, context.resolveValue(property.getType(), newValue));

		} catch (Exception e) {
			property.setDirectUnsafe(entity, originalValue);
			throw e;
		}
	}

}
