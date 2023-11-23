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
package com.braintribe.model.processing.manipulator.expert.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
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
import com.braintribe.model.generic.manipulation.VoidManipulation;
import com.braintribe.model.generic.manipulation.util.ManipulationBuilder;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.processing.manipulator.api.Manipulator;
import com.braintribe.model.processing.manipulator.api.ManipulatorContext;

public abstract class AbstractManipulatorContext implements ManipulatorContext {
	protected static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	protected Predicate<Manipulation> manipulationFilter;
	protected boolean valueResolution = true;
	
	protected Manipulator<CompoundManipulation> compoundManipulator = CompoundManipulator.defaultInstance;
	protected Manipulator<ManifestationManipulation> manifestionManipulator = NoopManipulator.getCastedDefaultInstance();
	protected Manipulator<InstantiationManipulation> instantiationManipulator = InstantiationManipulator.defaultInstance;
	protected Manipulator<DeleteManipulation> deleteManipulator = NoopManipulator.getCastedDefaultInstance();
	protected Manipulator<ChangeValueManipulation> changeValueManipulator = ChangeValueManipulator.defaultInstance;
	protected Manipulator<AbsentingManipulation> absentingManipulator = AbsentingManipulator.defaultInstance;
	protected Manipulator<AddManipulation> addManipulator = AddManipulator.defaultInstance;
	protected Manipulator<RemoveManipulation> bulkRemoveFromCollectionManipulator = RemoveManipulator.defaultInstance;
	protected Manipulator<ClearCollectionManipulation> clearCollectionManipulator = ClearCollectionManipulator.defaultInstance;
	protected Manipulator<VoidManipulation> voidManipulator = NoopManipulator.getCastedDefaultInstance();
	
	public void setManipulationFilter(Predicate<Manipulation> manipulationFilter) {
		this.manipulationFilter = manipulationFilter;
	}
	
	public void setValueResolution(boolean valueResolution) {
		this.valueResolution = valueResolution;
	}
	
	@Override
	public void apply(Manipulation manipulation) {
		if (manipulationFilter != null && manipulationFilter.test(manipulation))
			return;
		
		switch (manipulation.manipulationType()) {
			case COMPOUND:
				compoundManipulator.apply((CompoundManipulation) manipulation, this);
				return;

			case MANIFESTATION:
				manifestionManipulator.apply((ManifestationManipulation) manipulation, this);
				return;
			case INSTANTIATION:
				instantiationManipulator.apply((InstantiationManipulation) manipulation, this);
				return;

			case DELETE:
				deleteManipulator.apply((DeleteManipulation) manipulation, this);
				return;

			case CHANGE_VALUE:
				changeValueManipulator.apply((ChangeValueManipulation) manipulation, this);
				return;

			case ABSENTING:
				absentingManipulator.apply((AbsentingManipulation) manipulation, this);
				return;

			case ADD:
				addManipulator.apply((AddManipulation) manipulation, this);
				return;
			case REMOVE:
				bulkRemoveFromCollectionManipulator.apply((RemoveManipulation) manipulation, this);
				return;
			case CLEAR_COLLECTION:
				clearCollectionManipulator.apply((ClearCollectionManipulation) manipulation, this);
				return;

			case VOID:
				voidManipulator.apply((VoidManipulation) manipulation, this);
				return;

			default:
				throw new UnknownEnumException(manipulation.manipulationType());
		}
	}
	
	protected abstract GenericEntity resolveEntity(EntityReference entityReference);
	
	@Override
	public LocalEntityProperty resolveOwner(PropertyManipulation manipulation) {
		Owner owner = manipulation.getOwner();

		switch (owner.ownerType()) {
			case ENTITY_PROPERTY:
				EntityProperty ep = (EntityProperty) owner;
				GenericEntity entity = resolveValue(ep.getReference());
				
				return ManipulationBuilder.localEntityProperty(entity, ep.getPropertyName());

			case LOCAL_ENTITY_PROPERTY:
				return (LocalEntityProperty) owner;

			default:
				throw new GenericModelException("Unknown owener type: " + owner.ownerType() + ". Manipulation: " + manipulation);
		}
	}

	@Override
	public <T> T resolveValue(Object valueDescriptor) {
		return resolveValue(null, valueDescriptor);
	}

	@Override
	public <T> T resolveValue(GenericModelType type, Object valueDescriptor) {
		if (!valueResolution) {
			if (type == null)
				type = BaseType.INSTANCE;
			
			return (T)type.getValueSnapshot(valueDescriptor);
		}
		
		
		if (valueDescriptor == null)
			return (T)valueDescriptor;
		
		if (valueDescriptor instanceof EntityReference) {
			return (T) resolveEntity((EntityReference) valueDescriptor);
			
		} else if (valueDescriptor instanceof EnumReference) {
			return (T) resolveEnum((EnumReference) valueDescriptor);
		}
		else {
			
			if (valueDescriptor instanceof List<?>) {
				List<?> oldList = (List<?>)valueDescriptor;
				List<Object> newList = new ArrayList<>(oldList.size());
				for (Object element : oldList) {
					Object newElement = resolveValue(element);
					newList.add(newElement);
				}
				return (T) newList;
			} else if (valueDescriptor instanceof Map<?, ?>) {
				Map<?, ?> oldMap = (Map<?, ?>)valueDescriptor;
				Map<Object, Object> newMap = new HashMap<>(oldMap.size());
				for (Map.Entry<?, ?> entry : oldMap.entrySet()) {
					Object newKey = resolveValue(entry.getKey());
					Object newValue = resolveValue(entry.getValue());
					newMap.put(newKey, newValue);
				}
				return (T) newMap;
				
			} else if (valueDescriptor instanceof Set<?>) {
				Set<?> oldSet = (Set<?>)valueDescriptor;
				Set<Object> newSet = new HashSet<>(oldSet.size());
				for (Object element : oldSet) {
					Object newElement = resolveValue(element);
					newSet.add(newElement);
				}
				return (T) newSet;
				
			}
			
			return (T) valueDescriptor;
		}	
	}

	private Enum<?> resolveEnum(EnumReference enumRef) {
		EnumType enumType = typeReflection.getType(enumRef.getTypeSignature());
		
		return enumType.getEnumValue(enumRef.getConstant()); 
	}
}
