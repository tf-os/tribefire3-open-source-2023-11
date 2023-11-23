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
package com.braintribe.model.generic.validation.expert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.accessor.ExpertKey;
import com.braintribe.model.generic.manipulation.accessor.ExpertRegistry;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.validation.ValidatorResult;
import com.braintribe.model.generic.validation.exception.CompoundValidationException;
import com.braintribe.model.generic.validation.log.ValidationLog;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.extended.EntityMdDescriptor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

@SuppressWarnings("deprecation")
public class Predator{	

	private ExpertRegistry expertRegistry;
	private ValidationLog currentValidationLog;
	private PersistenceGmSession gmSession;
	private Set<Future<ValidatorResult>> resultSet;
	private Future<ValidationLog> validationLogFuture;
	private boolean allValidatorTriggered;
	private boolean futureSucceded;
	private CompoundValidationException compoundException;
		
	public ValidationLog getCurrentValidationLog() {
		return currentValidationLog;
	}
	
	public void setExpertRegistry(ExpertRegistry expertRegistry) {
		this.expertRegistry = expertRegistry;
	}

	public ExpertRegistry getExpertRegistry() {
		return expertRegistry;
	}
	
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	private void fillEntitySet(HashMap<GenericEntity, List<Manipulation>> entityMap,List<Manipulation> manipulations) {
		manipulations.forEach(manipulation -> fillEntitySet(entityMap, manipulation));
	}
	
	private void fillEntitySet(HashMap<GenericEntity, List<Manipulation>> entityMap,Manipulation manipulation) {
		if (manipulation instanceof CompoundManipulation) {
			fillEntitySet(entityMap,((CompoundManipulation) manipulation).getCompoundManipulationList());
			return;
		}

		if(manipulation instanceof DeleteManipulation) {
			entityMap.remove(((DeleteManipulation)manipulation).getEntity());
			return;
		}
		
        GenericEntity entityToAdd = null;		
		if (manipulation instanceof PropertyManipulation) {
			PropertyManipulation propertyManipulation = (PropertyManipulation) manipulation;
			entityToAdd = ((LocalEntityProperty) propertyManipulation.getOwner()).getEntity();
		} else if (manipulation instanceof InstantiationManipulation) {
			entityToAdd =((InstantiationManipulation)manipulation).getEntity();
		}
		
		if (entityToAdd != null) {
			List<Manipulation> listEntityManipulation;
			if (entityMap.containsKey(entityToAdd)) {
				listEntityManipulation = entityMap.get(entityToAdd);
			} else {
				listEntityManipulation = new ArrayList<>();
				entityMap.put(entityToAdd, listEntityManipulation);
			}
			listEntityManipulation.add(manipulation);
		}		
	}
	
	public Future<ValidationLog> validateManipulation(ValidationContext validationContext, Manipulation manipulation) {
		return validateManipulations(validationContext, Arrays.asList(manipulation));
	}
	
	public Future<ValidationLog> validateManipulations(ValidationContext validationContext, List<Manipulation> manipulations) {
		currentValidationLog = new ValidationLog();
		//HashSet<GenericEntity> entitySet = new HashSet<>();
		HashMap<GenericEntity, List<Manipulation>> entityMap = new HashMap<>(); 
		fillEntitySet(entityMap,manipulations);		
		validationLogFuture = new Future<>();
		resultSet  = new HashSet<>();
		compoundException = new CompoundValidationException();
		allValidatorTriggered = false;
		futureSucceded = false;
		for (final GenericEntity genericEntity : entityMap.keySet())
			validateEntity(genericEntity, validationContext, entityMap.get(genericEntity), false);
		allValidatorTriggered = true;
		
		if (!futureSucceded && resultSet.isEmpty()) {
			futureSucceded = true;
			validationLogFuture.onSuccess(currentValidationLog);
		}
		
		return validationLogFuture;	
	}
	
	public Future<ValidationLog> validateEntity(GenericEntity genericEntity, ValidationContext validationContext) {
		return validateEntity(genericEntity, validationContext, null);
	}

	public Future<ValidationLog> validateEntity(GenericEntity genericEntity, ValidationContext validationContext, List<Manipulation> listManipulation) {
		return validateEntity(genericEntity, validationContext, listManipulation, true);
	}
	
	private Future<ValidationLog> validateEntity(GenericEntity genericEntity, ValidationContext validationContext, List<Manipulation> listManipulation, boolean handleResult) {
	    return validateEntity(genericEntity, validationContext, listManipulation, null, null, handleResult);
	}
		
	private Future<ValidationLog> validateEntity(GenericEntity genericEntity, ValidationContext validationContext, List<Manipulation> listManipulation, String propertyName, Object propertyValue, boolean handleResult) {
		if (handleResult) {
			currentValidationLog = new ValidationLog();
			validationLogFuture = new Future<>();
			resultSet  = new HashSet<>();
			compoundException = new CompoundValidationException();
			allValidatorTriggered = false;
			futureSucceded = false;
		}
		
		EntityType<GenericEntity> entityType = genericEntity.entityType();
		List<Pair<MetaData, EntitySignatureAndPropertyName>> metadataPairsList = new ArrayList<>(); 

		ModelMdResolver modelMdResolver = gmSession.getModelAccessory().getMetaData();
		List<EntityMdDescriptor> entityTypeMetaDataList = modelMdResolver.entity(genericEntity).meta(MetaData.T).listExtended();
		
		for (EntityMdDescriptor entityMetadata : entityTypeMetaDataList)
			metadataPairsList.add(new Pair<>(entityMetadata.getResolvedValue(), new EntitySignatureAndPropertyName(entityType.getTypeSignature(), "")));

		for (Property property : filterProperties(entityType.getProperties())) {
			String filterPropertyName = property.getName();
			
			if (propertyName == null || propertyName.isEmpty() || filterPropertyName.equals(propertyName)) 			
				findMetaDataPairs(genericEntity, entityType, metadataPairsList, modelMdResolver, property, filterPropertyName);
		}
		
		for (Pair<MetaData, EntitySignatureAndPropertyName> pair : metadataPairsList) {
			MetaData metaData = pair.getFirst();
			Validator<MetaData> validator = expertRegistry.getExpert(Validator.class, metaData.entityType().getJavaType());
			if (validator == null)
				continue;
			
			validator.setGmSession(gmSession);
			Future<ValidatorResult> future;
			if (propertyName == null || propertyName.isEmpty())
				future = validator.validate(validationContext, genericEntity, metaData, pair.getSecond());
			else
				future = validator.validateValue(validationContext, genericEntity, metaData, pair.getSecond(), propertyValue);
			
			resultSet.add(future);
			
			future.andThen(result -> {
				if (listManipulation != null)
					result.getListManipulation().addAll(listManipulation);
				
				if (!result.getResult() || validationContext.isCompleteConfiguredValidation()) {
					if (currentValidationLog.get(genericEntity) != null)
						currentValidationLog.get(genericEntity).add(result);
					else {
						ArrayList<ValidatorResult> resultList = new ArrayList<>();
						resultList.add(result);
						currentValidationLog.put(genericEntity, resultList);
					}
				}
				
				resultSet.remove(future);
				if (resultSet.isEmpty() && allValidatorTriggered && !futureSucceded) {
					prepareGroupValidation(genericEntity, validationContext);
					
					futureSucceded = true;
					validationLogFuture.onSuccess(currentValidationLog);
				}
			}).onError(e -> {
				System.err.println("error while validateManipulations within Predator");
				compoundException.addThrowable(e);
				if (allValidatorTriggered)
					validationLogFuture.onFailure(compoundException);
			});
		}

		if (!futureSucceded && resultSet.isEmpty()) 
			prepareGroupValidation(genericEntity, validationContext);
		
		if (!handleResult)
			return null;
		
		allValidatorTriggered = true;
		
		if (!futureSucceded && resultSet.isEmpty()) {
			futureSucceded = true;
			validationLogFuture.onSuccess(currentValidationLog);
		}
		
		return validationLogFuture;	
	}

	private void prepareGroupValidation(GenericEntity genericEntity, ValidationContext validationContext) {
		if (!validationContext.isGroupValidations())
			return;
		
		List<ValidatorResult> groupListValidatorResult = ValidationGroupMessageExpert.prepareGroupValidatorResult(currentValidationLog.get(genericEntity));
		if (groupListValidatorResult != null) {
			currentValidationLog.get(genericEntity).clear();
			currentValidationLog.get(genericEntity).addAll(groupListValidatorResult);
		}
	}

	public Future<ValidationLog> validatePropertyValue(GenericEntity genericEntity, ValidationContext validationContext, String propertyName, Object propertyValue) {
	    return validatePropertyValue(genericEntity, validationContext, null, propertyName, propertyValue);
	}	

	public Future<ValidationLog> validatePropertyValue(GenericEntity genericEntity, ValidationContext validationContext, List<Manipulation> listManipulation, String propertyName, Object propertyValue) {
	    return validateEntity(genericEntity, validationContext, listManipulation, propertyName, propertyValue, true);
	}	
	
	private void findMetaDataPairs(GenericEntity genericEntity, EntityType<GenericEntity> entityType,
			List<Pair<MetaData, EntitySignatureAndPropertyName>> metadataPairsList, ModelMdResolver modelMdResolver,
			Property property, String propertyName) {
		for (ExpertKey key : expertRegistry.getExpertMap().keySet()) {
			MetaData entityTypePropertyMetaData = modelMdResolver
					.entity(genericEntity)
					.property(property.getName())
					.meta(GMF.getTypeReflection().getEntityType((Class<? extends GenericEntity>) key.getHandledClass()))
					.exclusive();
			if (entityTypePropertyMetaData != null)
				metadataPairsList.add(new Pair<>(entityTypePropertyMetaData, new EntitySignatureAndPropertyName(entityType.getTypeSignature(), propertyName)));
		}
	}
		
	private List<Property> filterProperties(List<Property> properties){		
		return properties.stream().filter((property) -> {
			return !property.getName().toLowerCase().equals(GenericEntity.globalId.toLowerCase());
		}).collect(Collectors.toList());		
	}
	
}
