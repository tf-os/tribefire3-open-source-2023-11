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

import java.util.Collection;
import java.util.Map;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.validation.ValidatorResult;
import com.braintribe.model.generic.validation.util.ValidatorUtil;
import com.braintribe.model.meta.data.constraint.Mandatory;

public class MandatoryValidator extends Validator<Mandatory> {
	
	private ValidatorResult validatorResult;
	
	@Override
	public Future<ValidatorResult> validate(ValidationContext validationContext, GenericEntity entity, Mandatory propertyMetaData,
			final EntitySignatureAndPropertyName entitySignatureAndPropertyName) {
		String propertyName = prepareValidation(validationContext, entity, entitySignatureAndPropertyName);
		
		if (!propertyMetaData.isTrue()) {
			validatorResult.setResult(true);
			return new Future<>(validatorResult);
		}
		
		EntityType<GenericEntity> type = entity.entityType();
		Property property = type.getProperty(propertyName);
		if (!ValidatorUtil.checkPropertyAbsense(entity, propertyName)) {
			Object value = property.get(entity);
			checkValue(property, value);
			return new Future<>(validatorResult);
		}
		
		Future<ValidatorResult> future = new Future<>();
		ValidatorUtil.fetchAbsentValue(validationContext, entity, propertyName).andThen(result -> {
			checkValue(property, result);
			future.onSuccess(validatorResult);
		}).onError(future::onFailure);
		
		return future;
	}

	private String prepareValidation(ValidationContext validationContext, GenericEntity entity,
			EntitySignatureAndPropertyName entitySignatureAndPropertyName) {
		String propertyName = entitySignatureAndPropertyName.getPropertyName();
		validatorResult = ValidatorUtil.prepareValidatorResult(propertyName);
		
		String propertyDisplayName = ValidatorUtil.getPropertyDisplayName(getGmSession(), entity, propertyName);
		
		String message;
		if (validationContext.isShortMessageStyle())
			message = LocalizedText.INSTANCE.stringDenyEmpty();			
		else	
			message = LocalizedText.INSTANCE.mandatoryMessage(propertyDisplayName);
		validatorResult.setMessageType(ValidatorMessageType.mandatory);
		validatorResult.setResultMessage(message);
		
		return propertyName;
	}

	@Override
	public Future<ValidatorResult> validateValue(ValidationContext validationContext, GenericEntity entity,
			Mandatory propertyMetaData, EntitySignatureAndPropertyName entitySignatureAndPropertyName, Object value) {
		String propertyName = prepareValidation(validationContext, entity, entitySignatureAndPropertyName);
		
		if (!propertyMetaData.isTrue()) {
			validatorResult.setResult(true);
			return new Future<>(validatorResult);
		}
		
		EntityType<GenericEntity> type = entity.entityType();
		Property property = type.getProperty(propertyName);

		checkValue(property, value);
		return new Future<>(validatorResult);
	}
	
	private void checkValue(Property property, Object value) {
		if (!property.getType().isCollection())
			validatorResult.setResult(value != null);
		else {
			int collectionSize = 0;
			if (value != null) {
				if (value instanceof Collection)
					collectionSize = ((Collection<?>) value).size();
				else
					collectionSize = ((Map<?,?>) value).size();
			}
			
			validatorResult.setResult(collectionSize > 0);
		}
	}

}
