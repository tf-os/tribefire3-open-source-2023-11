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

import static com.braintribe.model.generic.builder.vd.VdBuilder.referenceWithNewPartition;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EnumReference;

/**
 * 
 */
class ReferenceResolver {
	static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	final List<AtomicManipulation> manipulations;
	final Map<GenericEntity, EntityReference> currentReference;
	final Map<Enum<?>, EnumReference> enumReference;

	final boolean globalReferences;

	public ReferenceResolver(List<AtomicManipulation> manipulations, Map<GenericEntity, EntityReference> initialReferences,
			boolean globalReferences) {
		this.manipulations = manipulations;
		this.globalReferences = globalReferences;
		this.currentReference = resolveInitialReferences(initialReferences);
		this.enumReference = new HashMap<Enum<?>, EnumReference>();
	}

	private Map<GenericEntity, EntityReference> resolveInitialReferences(Map<GenericEntity, EntityReference> initialReferences) {
		Map<GenericEntity, EntityReference> result = newMap();

		for (AtomicManipulation manipulation : manipulations) {
			if (manipulation.manipulationType() == ManipulationType.CHANGE_VALUE) {
				ChangeValueManipulation cvm = (ChangeValueManipulation) manipulation;
				LocalEntityProperty lep = (LocalEntityProperty) cvm.getOwner();
				if (lep == null)
					continue;

				GenericEntity entity = lep.getEntity();
				if (result.containsKey(entity))
					continue;

				// Partition is not handled here on purpose

				if (globalReferences) {
					if (GenericEntity.globalId.equals(lep.getPropertyName())) {
						String oldGlobalId = (String) ((ChangeValueManipulation) cvm.getInverseManipulation()).getNewValue();

						// this ensure that if oldGlobalId was null, we'd get a PreliminaryEntityReference back
						EntityReference ref = entity.entityType().createGlobalReference(entity, oldGlobalId);
						result.put(entity, ref);
					}

				} else {

					if (GenericEntity.id.equals(lep.getPropertyName())) {
						Object oldId = ((ChangeValueManipulation) cvm.getInverseManipulation()).getNewValue();
						EntityReference ref = entity.entityType().createReference(entity, oldId);

						result.put(entity, ref);
					}
				}
			}
		}

		if (initialReferences != null)
			result.putAll(initialReferences);

		return result;
	}

	public EntityReference getReference(GenericEntity entity) {
		EntityReference result = currentReference.get(entity);

		if (result != null) {
			return result;
		}

		result = globalReferences ? entity.globalReference() : entity.reference();
		currentReference.put(entity, result);

		return result;
	}

	public EnumReference getReference(Enum<?> enumConstant) {
		EnumReference result = enumReference.get(enumConstant);

		if (result != null) {
			return result;
		}

		Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) enumConstant.getClass();
		result = typeReflection.getEnumType(enumClass).getEnumReference(enumConstant);
		enumReference.put(enumConstant, result);

		return result;
	}

	public void onLocalManipulationProcessed(AtomicManipulation manipulation) {
		if (manipulation.manipulationType() == ManipulationType.CHANGE_VALUE) {
			ChangeValueManipulation cvm = (ChangeValueManipulation) manipulation;
			LocalEntityProperty lep = (LocalEntityProperty) cvm.getOwner();
			GenericEntity entity = lep.getEntity();
			EntityType<GenericEntity> entityType = entity.entityType();

			if (globalReferences) {
				if (GenericEntity.globalId.equals(lep.getPropertyName())) {
					String newGlobalId = (String) cvm.getNewValue();
					EntityReference reference = entityType.createGlobalReference(entity, newGlobalId);
					currentReference.put(entity, reference);
				}

			} else {
				if (GenericEntity.id.equals(lep.getPropertyName())) {
					Object newId = cvm.getNewValue();
					EntityReference reference = entityType.createReference(entity, newId);
					EntityReference oldRef = currentReference.get(entity);
					reference.setRefPartition(oldRef.getRefPartition());
					currentReference.put(entity, reference);

				} else if (GenericEntity.partition.equals(lep.getPropertyName())) {
					/* This is stupid, but we have to do this when remotifying induced manipulations - imagine both
					 * partition and id are set, in this order - then the id change has to use a reference with partition
					 * being set. Once we change the stack to use exactly one manipulation, this will not be needed. */
					String newPartition = (String) cvm.getNewValue();
					if (newPartition == null)
						/* This is just a minor hack to avoid problems when partition is assigned to null and then to a
						 * value for new entities - because we say the partition should be there the whole time, even
						 * for instantiation, this would break something. It's all a mess. */
						return; 
					
					EntityReference oldRef = currentReference.get(entity);
					EntityReference newRef = referenceWithNewPartition(oldRef, newPartition);

					currentReference.put(entity, newRef);
				}
			}
		}
	}

}
