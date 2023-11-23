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
package com.braintribe.model.processing.manipulation.lab;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.value.EntityReference;

/**
 * @author peter.gazdik
 */
public class ManipulationFilter {

	public static List<AtomicManipulation> filterByOwnerType(List<AtomicManipulation> manipulations, String signature) {
		return filterByOwnerTypes(manipulations, asSet(signature));
	}

	public static List<AtomicManipulation> filterByOwnerTypes(List<AtomicManipulation> manipulations, Set<String> signatures) {
		List<AtomicManipulation> result = newList();

		for (AtomicManipulation am: manipulations) {
			if (hasOwnerTypeOneOf(am, signatures)) {
				result.add(am);
			}
		}

		return result;
	}

	private static boolean hasOwnerTypeOneOf(AtomicManipulation am, Set<String> signatures) {
		return signatures.contains(getOwnerTypeSignature(am));
	}

	private static String getOwnerTypeSignature(AtomicManipulation am) {
		switch (am.manipulationType()) {
			case ADD:
			case CHANGE_VALUE:
			case CLEAR_COLLECTION:
			case REMOVE:
				return getSignatureFromOwner(((PropertyManipulation) am).getOwner());
			case DELETE:
				return getSignatureFromReference(((DeleteManipulation) am).getEntity());
			case INSTANTIATION:
				return getSignatureFromReference(((InstantiationManipulation) am).getEntity());
			default:
				throw new RuntimeException("Unexpected manipulation: " + am);
		}
	}

	private static String getSignatureFromOwner(Owner owner) {
		return ((EntityProperty) owner).getReference().getTypeSignature();
	}

	private static String getSignatureFromReference(GenericEntity entity) {
		return ((EntityReference) entity).getTypeSignature();
	}

}
