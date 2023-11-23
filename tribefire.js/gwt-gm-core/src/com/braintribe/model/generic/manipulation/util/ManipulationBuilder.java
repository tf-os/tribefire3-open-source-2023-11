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
package com.braintribe.model.generic.manipulation.util;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AbsentingManipulation;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.ManifestationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationComment;
import com.braintribe.model.generic.manipulation.NormalizedCompoundManipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.manipulation.VoidManipulation;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.value.EntityReference;

/**
 * 
 * @author peter.gazdik
 */
public class ManipulationBuilder {

	public static InstantiationManipulation instantiation(GenericEntity entity) {
		InstantiationManipulation result = InstantiationManipulation.T.create();
		result.setEntity(entity);

		return result;
	}

	public static DeleteManipulation delete(GenericEntity entity, DeleteMode deleteMode) {
		DeleteManipulation result = DeleteManipulation.T.create();
		result.setDeleteMode(deleteMode);
		result.setEntity(entity);

		return result;
	}

	public static AddManipulation add(Object key, Object value, Owner owner) {
		return addManipulation(key, value, owner);
	}

	public static AddManipulation addManipulation(Object key, Object value, Owner owner) {
		return addManipulation(asMap(key, value), owner);
	}

	public static AddManipulation add(Map<Object, Object> itemsToAdd, Owner owner) {
		return addManipulation(itemsToAdd, owner);
	}

	public static AddManipulation addManipulation(Map<Object, Object> itemsToAdd, Owner owner) {
		AddManipulation result = AddManipulation.T.create();
		result.setItemsToAdd(itemsToAdd);
		result.setOwner(owner);

		return result;
	}

	public static ChangeValueManipulation changeValue(Object newValue, Owner owner) {
		ChangeValueManipulation result = ChangeValueManipulation.T.create();
		result.setOwner(owner);
		result.setNewValue(newValue);

		return result;
	}

	public static RemoveManipulation remove(Object key, Object value, Owner owner) {
		return removeManipulation(key, value, owner);
	}

	public static RemoveManipulation removeManipulation(Object key, Object value, Owner owner) {
		Map<Object, Object> itemsToRemove = new HashMap<Object, Object>();
		itemsToRemove.put(key, value);

		return removeManipulation(itemsToRemove, owner);
	}

	public static RemoveManipulation remove(Map<Object, Object> itemsToRemove, Owner owner) {
		return removeManipulation(itemsToRemove, owner);
	}

	public static RemoveManipulation removeManipulation(Map<Object, Object> itemsToRemove, Owner owner) {
		RemoveManipulation result = RemoveManipulation.T.create();
		result.setItemsToRemove(itemsToRemove);
		result.setOwner(owner);

		return result;
	}

	public static ClearCollectionManipulation clear(Owner owner) {
		return clearManipulation(owner);
	}

	public static ClearCollectionManipulation clearManipulation(Owner owner) {
		ClearCollectionManipulation result = ClearCollectionManipulation.T.create();
		result.setOwner(owner);

		return result;
	}

	public static ManifestationManipulation manifestation(GenericEntity entity) {
		ManifestationManipulation result = ManifestationManipulation.T.create();
		result.setEntity(entity);

		return result;
	}

	public static AbsentingManipulation absenting(AbsenceInformation absenceInformation, Owner owner) {
		AbsentingManipulation result = AbsentingManipulation.T.create();
		result.setAbsenceInformation(absenceInformation);
		result.setOwner(owner);

		return result;
	}

	public static CompoundManipulation compound(Manipulation... manipulations) {
		return compound(asList(manipulations));
	}
	
	public static CompoundManipulation compound(List<? extends Manipulation> manipulations) {
		CompoundManipulation result = compound();
		result.setCompoundManipulationList((List<Manipulation>) manipulations);

		return result;
	}

	public static CompoundManipulation compound() {
		return CompoundManipulation.T.create();
	}

	public static NormalizedCompoundManipulation normalizedCompound(List<Manipulation> manipulations) {
		NormalizedCompoundManipulation result = normalizedCompound();
		result.setCompoundManipulationList(manipulations);

		return result;
	}

	public static NormalizedCompoundManipulation normalizedCompound() {
		return NormalizedCompoundManipulation.T.create();
	}

	public static VoidManipulation voidManipulation() {
		return VoidManipulation.T.create();
	}

	public static EntityProperty entityProperty(EntityReference ref, String propertyName) {
		EntityProperty result = EntityProperty.T.create();
		result.setReference(ref);
		result.setPropertyName(propertyName);

		return result;
	}

	public static LocalEntityProperty localEntityProperty(GenericEntity entity, String propertyName) {
		LocalEntityProperty result = LocalEntityProperty.T.create();
		result.setEntity(entity);
		result.setPropertyName(propertyName);

		return result;
	}

	public static ManipulationComment comment(String text) {
		ManipulationComment result = ManipulationComment.T.create();
		result.setText(text);

		return result;
	}

	public static ManipulationComment comment(String text, Date date) {
		ManipulationComment result = ManipulationComment.T.createRaw();
		result.setText(text);
		result.setDate(date);
		
		return result;
	}

}
