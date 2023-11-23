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
package com.braintribe.model.processing.manipulation.basic.tools.clone;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.model.generic.manipulation.AbsentingManipulation;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.ManifestationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;

/**
 * @author peter.gazdik
 */
public class ManipulationCloner {

	public static <M extends Manipulation> M clone(M original, boolean handleInverse) {
		if (handleInverse)
			throw new UnsupportedOperationException("Cloning manipulations with their inverses is not supported yet.");

		return (M) copy(original, handleInverse);
	}

	private static Manipulation copy(Manipulation m, boolean handleInverse) {
		switch (m.manipulationType()) {
			case ABSENTING:
				return copy((AbsentingManipulation) m);
			case ADD:
				return copy((AddManipulation) m);
			case CHANGE_VALUE:
				return copy((ChangeValueManipulation) m);
			case CLEAR_COLLECTION:
				return copy((ClearCollectionManipulation) m);
			case COMPOUND:
				return copy((CompoundManipulation) m, handleInverse);
			case DELETE:
				return copy((DeleteManipulation) m);
			case INSTANTIATION:
				return copy((InstantiationManipulation) m);
			case MANIFESTATION:
				return copy((ManifestationManipulation) m);
			case REMOVE:
				return copy((RemoveManipulation) m);
			case VOID:
				return copyManipulation(m);
			default:
				throw new UnknownEnumException(m.manipulationType());
		}
	}

	private static Manipulation copy(AbsentingManipulation original) {
		AbsentingManipulation result = copyPropertyManipulation(original);
		result.setAbsenceInformation(original.getAbsenceInformation());

		return result;
	}

	private static Manipulation copy(AddManipulation original) {
		AddManipulation result = copyPropertyManipulation(original);
		result.setItemsToAdd(new HashMap<>(original.getItemsToAdd()));

		return result;
	}

	private static Manipulation copy(ChangeValueManipulation original) {
		ChangeValueManipulation result = copyPropertyManipulation(original);
		result.setNewValue(copyValue(original.getNewValue()));

		return result;
	}

	private static Object copyValue(Object newValue) {
		if (newValue == null)
			return null;

		if (newValue instanceof Collection) {
			if (newValue instanceof Set)
				return newSet((Set<?>) newValue);
			else
				return newList((List<?>) newValue);
		}
		
		if (newValue instanceof Map)
			return newMap((Map<?, ?>) newValue);
		
		return newValue;
	}

	private static Manipulation copy(ClearCollectionManipulation original) {
		ClearCollectionManipulation result = copyPropertyManipulation(original);

		return result;
	}

	private static Manipulation copy(CompoundManipulation original, boolean handleInverse) {
		CompoundManipulation result = copyManipulation(original);
		result.setCompoundManipulationList(copy(original.getCompoundManipulationList(), handleInverse));

		return result;
	}

	private static List<Manipulation> copy(List<Manipulation> list, boolean handleInverse) {
		if (list == null)
			return null;

		return list.stream() //
				.map(m -> clone(m, handleInverse)) //
				.collect(Collectors.toList());
	}

	private static Manipulation copy(DeleteManipulation original) {
		DeleteManipulation result = copyManipulation(original);
		result.setEntity(original.getEntity());
		result.setDeleteMode(original.getDeleteMode());

		return result;
	}

	private static Manipulation copy(InstantiationManipulation original) {
		InstantiationManipulation result = copyManipulation(original);
		result.setEntity(original.getEntity());

		return result;
	}

	private static Manipulation copy(ManifestationManipulation original) {
		ManifestationManipulation result = copyManipulation(original);
		result.setEntity(original.getEntity());

		return result;
	}

	private static Manipulation copy(RemoveManipulation original) {
		RemoveManipulation result = copyPropertyManipulation(original);
		result.setItemsToRemove(new HashMap<>(original.getItemsToRemove()));

		return result;
	}

	private static <M extends PropertyManipulation> M copyPropertyManipulation(M original) {
		M copy = copyManipulation(original);
		copy.setOwner(copyOwner(original.getOwner()));

		return copy;
	}

	private static <M extends Manipulation> M copyManipulation(M original) {
		M copy = original.<M> entityType().createPlain();
		copy.setId(original.getId());

		return copy;
	}

	private static Owner copyOwner(Owner owner) {
		switch (owner.ownerType()) {
			case ENTITY_PROPERTY:
				return copyEntityProperty((EntityProperty) owner);
			case LOCAL_ENTITY_PROPERTY:
				return copyEntityProperty((LocalEntityProperty) owner);
		}

		throw new IllegalArgumentException("Unsupported owner " + owner + " of type: " + owner.ownerType());
	}

	private static Owner copyEntityProperty(EntityProperty ep) {
		EntityProperty result = EntityProperty.T.createPlain();
		result.setPropertyName(ep.getPropertyName());
		result.setReference(ep.getReference());

		return result;
	}

	private static Owner copyEntityProperty(LocalEntityProperty lep) {
		LocalEntityProperty result = LocalEntityProperty.T.createPlain();
		result.setPropertyName(lep.getPropertyName());
		result.setEntity(lep.getEntity());

		return result;
	}
}
