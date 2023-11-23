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
package com.braintribe.model.processing.manipulation.basic.tools;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static java.util.Collections.emptyList;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.builder.vd.VdBuilder;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.util.ManipulationBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;

/**
 * 
 */
public class ManipulationTools {

	public static List<AtomicManipulation> inline(List<? extends Manipulation> manipulations) {
		if (isEmpty(manipulations))
			return emptyList();

		return manipulations.stream() //
				.flatMap(Manipulation::stream) //
				.collect(Collectors.toList());
	}

	public static void visit(Manipulation m, Consumer<? super AtomicManipulation> consumer) {
		if (m.manipulationType() != ManipulationType.COMPOUND)
			consumer.accept((AtomicManipulation) m);
		else {
			List<Manipulation> mList = ((CompoundManipulation) m).getCompoundManipulationList();
			for (Manipulation mm : nullSafe(mList))
				visit(mm, consumer);
		}
	}

	/**
	 * Creates a {@link Manipulation} instance based on the passed list of {@link Manipulation}. <br/>
	 * If the list contains one element the Manipulation itself will be returned. <br/>
	 * If the list contains no element null will be returned. <br/>
	 * If more then one elements are in the list a {@link CompoundManipulation} containing the list will be returned.
	 */
	public static Manipulation asManipulation(List<? extends Manipulation> list) {
		switch (list.size()) {
			case 0:
				return null;
			case 1:
				return list.get(0);
			default:
				return compound(list);
		}
	}

	public static ManipulationRequest asManipulationRequest(Manipulation manipulation) {
		ManipulationRequest result = ManipulationRequest.T.create();
		result.setManipulation(manipulation);

		return result;
	}

	public static ManipulationResponse asManipulationResponse(Manipulation inducedManipulation) {
		ManipulationResponse result = ManipulationResponse.T.create();
		result.setInducedManipulation(inducedManipulation);

		return result;
	}

	/**
	 * Combines all passed manipulations to a list and returns the result of {@link #asManipulation(List)} with this list passed. <br />
	 * All top-level {@link CompoundManipulation}s are flattened and only their direct elements are put to the list.<br />
	 * Top-level null arguments are ignored.
	 */
	public static Manipulation combine(Manipulation... manipulations) {
		List<Manipulation> list = newList();
		for (Manipulation m : manipulations) {
			if (m != null) {
				if (m.manipulationType() == ManipulationType.COMPOUND) {
					list.addAll(((CompoundManipulation) m).getCompoundManipulationList());
				} else {
					list.add(m);
				}
			}
		}
		return asManipulation(list);
	}

	public static Manipulation createInverse(List<? extends Manipulation> manipulations) {
		switch (manipulations.size()) {
			case 0:
				return null;
			case 1:
				return first(manipulations).getInverseManipulation();
			default:
				return compound(createInverses(manipulations));
		}
	}

	public static CompoundManipulation createInverse(CompoundManipulation cm) {
		CompoundManipulation result = ManipulationBuilder.compound();

		List<Manipulation> manipulations = cm.getCompoundManipulationList();
		if (manipulations != null) {
			List<Manipulation> inverses = createInverses(manipulations);
			result.setCompoundManipulationList(inverses);
		}

		return result;
	}

	private static List<Manipulation> createInverses(List<? extends Manipulation> manipulations) {
		List<Manipulation> inverses = newList(manipulations.size());
		for (int i = manipulations.size() - 1; i >= 0; i--)
			inverses.add(manipulations.get(i).getInverseManipulation());

		return inverses;
	}

	/**
	 * Creates a new {@link EntityReference} based on an existing one with one of the identifying properties (id or partition) being changed. This is
	 * useful when processing manipulations and a {@link ChangeValueManipulation} on one of these properties is encountered.
	 */
	public static EntityReference createUpdatedReference(EntityReference originalRef, String idOrPartitionProperty, Object newValue) {
		boolean idChanged = GenericEntity.id.equals(idOrPartitionProperty);
		boolean persistent = originalRef instanceof PersistentEntityReference || idChanged;

		Object newId = idChanged ? newValue : originalRef.getRefId();
		String newPartition = idChanged ? originalRef.getRefPartition() : (String) newValue;

		return newReference(persistent, originalRef.getTypeSignature(), newId, newPartition);
	}

	public static EntityReference newReference(boolean persistent, EntityType<?> entityType, Object id, String partition) {
		return newReference(persistent, entityType.getTypeSignature(), id, partition);
	}

	public static EntityReference newReference(boolean persistent, String typeSignature, Object id, String partition) {
		if (persistent)
			return VdBuilder.persistentReference(typeSignature, id, partition);
		else
			return VdBuilder.preliminaryReference(typeSignature, id, partition);
	}

	public static EntityReference newGlobalReference(boolean global, EntityType<?> entityType, Object id) {
		return newGlobalReference(global, entityType.getTypeSignature(), id);
	}

	public static EntityReference newGlobalReference(boolean global, String typeSignature, Object id) {
		if (global)
			return VdBuilder.globalReference(typeSignature, id);
		else
			return VdBuilder.preliminaryReference(typeSignature, id, null);
	}

}
