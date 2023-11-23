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
package com.braintribe.model.generic.sync;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.entityProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.DelegatingCloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.GmReflectionTools;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyTransferCompetence;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StandardTraversingContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.tracking.ManipulationTracking;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;


public class EntitySynchronization {
	private Function<EntityReference, GenericEntity> existingEntityProvider;
	private GenericModelTypeReflection typeReflection;
	private Map<EntityType<?>, Boolean> synchronizationCandidates = new HashMap<EntityType<?>, Boolean>();
	private Function<EntityProperty, Boolean> manipulatedPropertiesFilter;
	
	public void setTypeReflection(
			GenericModelTypeReflection genericModelTypeReflection) {
		this.typeReflection = genericModelTypeReflection;
	}
	
	public void setManipulatedPropertiesFilter(
			Function<EntityProperty, Boolean> manipulatedPropertiesFilter) {
		this.manipulatedPropertiesFilter = manipulatedPropertiesFilter;
	}
	
	
	public void setExistingEntityProvider(
			Function<EntityReference, GenericEntity> existingEntityProvider) {
		this.existingEntityProvider = existingEntityProvider;
	}
	
	
	private class IdentityManagementCloningContext extends StandardCloningContext implements PropertyTransferCompetence {
		private SynchronizationContext synchronizationContext;
		
		public IdentityManagementCloningContext(
				SynchronizationContext synchronizationContext) {
			this.synchronizationContext = synchronizationContext;
		}

		@Override
		public void transferProperty(EntityType<?> sourceEntityType,
				GenericEntity sourceEntity, GenericEntity targetEntity,
				Property property, Object propertyValue)
				throws GenericModelException {
			Object oldPropertyValue = property.get(targetEntity);
			
			if (!GmReflectionTools.equals(oldPropertyValue, propertyValue))
				property.set(targetEntity, propertyValue);
		}

		@Override
		public boolean canTransferPropertyValue(
				EntityType<? extends GenericEntity> entityType,
				Property property, GenericEntity instanceToBeCloned,
				GenericEntity clonedInstance,
				AbsenceInformation sourceAbsenceInformation) {
			try {
				EntityReference entityReference = instanceToBeCloned.reference();
				GenericEntity entity = existingEntityProvider.apply(entityReference);
				if(entity == null){
					return true;
				}else{
					if(sourceAbsenceInformation != null)
						return false;
					EntityProperty entityProperty = entityProperty(entityReference, property.getName());
					return !manipulatedPropertiesFilter.apply(entityProperty);
				}
			} catch (RuntimeException e) {
				throw new RuntimeException("error while checking for manipulated properties", e);
			}		
		}
		
		
		
		@Override
		public GenericEntity supplyRawClone(
				EntityType<? extends GenericEntity> entityType,
				GenericEntity instanceToBeCloned) {
			try {
				EntityReference reference = instanceToBeCloned.reference();
				GenericEntity existingEntity = existingEntityProvider.apply(reference);
				
				if (existingEntity != null)
					return existingEntity;
				else {
					GenericEntity newInstance = super.supplyRawClone(entityType, instanceToBeCloned);
					synchronizationContext.registerNewEntity(newInstance);
					return newInstance;
				}
			} catch (RuntimeException e) {
				throw new RuntimeException("error while supplying rawclone", e);
			}
		}
	}
	
	public SynchronizationContext syncAndRefresh(Object genericModelValue) {
		final SynchronizationContext synchronizationContext = new SynchronizationContext();
		IdentityManagementCloningContext syncloningContext = new IdentityManagementCloningContext(synchronizationContext);
		Object syncedValue;
		try {
			ManipulationTracking.getTracker().begin();
			BaseType baseType = typeReflection.getBaseType();
			syncedValue = baseType.clone(syncloningContext, genericModelValue, StrategyOnCriterionMatch.skip);
			List<Manipulation> manipulations = ManipulationTracking.getTracker().getCurrentManipulationCollector().getManipulations();
			
			List<Manipulation> filteredManipulations = new ArrayList<Manipulation>();
			filterRefreshManipulations(manipulations, filteredManipulations);
			
			synchronizationContext.getUpdateManipulation().setCompoundManipulationList(filteredManipulations);
		}
		finally {
			ManipulationTracking.getTracker().stop();
		}
		
		synchronizationContext.setSyncedResult(syncedValue);
		return synchronizationContext;
	}
	
	protected void filterRefreshManipulations(List<Manipulation> manipulations, List<Manipulation> filteredManipulations) {
		try {
			for (Manipulation manipulation: manipulations){
				if (manipulation instanceof CompoundManipulation) {
					CompoundManipulation compoundManipulation = (CompoundManipulation)manipulation;
					List<Manipulation> compoundManipulationList = compoundManipulation.getCompoundManipulationList();
					if (compoundManipulationList != null)
						filterRefreshManipulations(compoundManipulationList, filteredManipulations);
				}
				else if (manipulation instanceof PropertyManipulation) {
					PropertyManipulation propertyManipulation = (PropertyManipulation)manipulation;
					EntityProperty entityProperty = (EntityProperty)propertyManipulation.getOwner();
					if (existingEntityProvider.apply(entityProperty.getReference()) != null) {
						filteredManipulations.add(manipulation);
					}
				}
			}
		} catch (RuntimeException e) {
			throw new RuntimeException("error while filtering refresh manipulations", e);
		}
	}
	
	public SynchronizationContext sync(String typeSignature, Object value) {
		GenericModelType type = typeReflection.getType(typeSignature);
	
		final SynchronizationContext synchronizationContext = new SynchronizationContext();
		
		TraversingContext cloningContext = new StandardTraversingContext() {
			
			@Override
			public void registerAsVisited(GenericEntity entity, Object associate) {
				synchronizationContext.registerNewEntity((GenericEntity)associate);
				super.registerAsVisited(entity, associate);
			}
			
			@Override
			public <A> A getAssociated(GenericEntity entity) {
				A associated = (A)super.getAssociated(entity);
				if (associated != null) 
					return associated;
				else {
					try {
						EntityType<GenericEntity> entityType = entity.entityType();		
						PersistentEntityReference persistentEntityReference = (PersistentEntityReference) entity.reference();
						GenericEntity existingEntity = existingEntityProvider.apply(persistentEntityReference);
						if (existingEntity != null) {
							super.registerAsVisited(entity, existingEntity);
						}
						else if (!isSynchronizationCandidateEntity(entityType)) {
							existingEntity = entity;
							super.registerAsVisited(entity, entity);
						}
						
						return (A)existingEntity;
					} catch (RuntimeException e) {
						throw new GenericModelException("error while synchronizing entities");
					}
				}
			}
		};
		
		Object syncedValue = type.clone(new DelegatingCloningContext(cloningContext), value, StrategyOnCriterionMatch.reference);
		synchronizationContext.setSyncedResult(syncedValue);
		return synchronizationContext;
	}
	
	protected boolean isSynchronizationCandidateEntity(EntityType<?> entityType) {
		Boolean candidate = synchronizationCandidates.get(entityType);
		
		if (candidate == null) {
			candidate = false;
			for (Property property: entityType.getProperties()) {
				if (isSynchronizationCandidateProperty(property)) {
					candidate = true;
					break;
				}
			}
			synchronizationCandidates.put(entityType, candidate);
		}
		
		return candidate;
	}
	
	protected boolean isSynchronizationCandidateProperty(Property property) {
		GenericModelType type = property.getType();
		if (type instanceof CollectionType) {
			return isSynchronizationCandidateCollection((CollectionType)type);
		}
		else
			return false;
	}
	
	/**
	 * @param collectionType
	 *            the type
	 */
	protected boolean isSynchronizationCandidateCollection(CollectionType collectionType) {
		GenericModelType parameterization[] = collectionType.getParameterization();
		
		for (GenericModelType parameterType: parameterization) {
			if (parameterType instanceof EntityType<?>)
				return true;
			else if (parameterType instanceof CollectionType) {
				CollectionType subCollectionType = (CollectionType)parameterType;
				if (isSynchronizationCandidateCollection(subCollectionType))
					return true;
			}
			else if (parameterType instanceof BaseType) {
				return true;
			}
		}
		
		return false;
	}
	
}
