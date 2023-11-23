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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.validation.ValidatorResult;
import com.braintribe.model.generic.validation.util.ValidatorUtil;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.ElementMaxLength;
import com.braintribe.model.meta.data.constraint.ElementMinLength;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.meta.data.constraint.MinLength;

public abstract class AbstractValidator<T extends MetaData> extends Validator<T> {
	
	protected Future<ValidatorResult> evaluateMetadata(ValidationContext validationContext, GenericEntity entity, String propertyName,
			String propertyDisplayName, T metadata, Object value) {
		ValidatorResult validatorResult = ValidatorUtil.prepareValidatorResult(propertyName);
		Property property = entity.entityType().getProperty(propertyName);
		
		if (metadata instanceof MaxLength || metadata instanceof MinLength && property.getType().getJavaType().equals(String.class)) {
			String stringValue = (String) value;
			int stringLength = stringValue != null ? stringValue.length() : 0;
			validatorResult.setResult(isLengthMetadataRespected(metadata, stringLength, true));
			validatorResult.setResultMessage(getStringResultMessage(validationContext, metadata, propertyDisplayName));
			validatorResult.setMessageType(getValidatorMessageType(metadata));
			validatorResult.setMessageParameter(String.valueOf(getLength(metadata)));
			return new Future<>(validatorResult);
		}
		
		if (!isCollectionProperty(property)) {
			validatorResult.setResult(true);
			return new Future<>(validatorResult);
		}

		if (!ValidatorUtil.checkPropertyAbsense(entity, propertyName)) {
			if (metadata instanceof MaxLength || metadata instanceof MinLength)
				evaluateCollectionCount(propertyDisplayName, validatorResult, value, metadata);
			else
				evaluateCollectionElements(propertyDisplayName, validatorResult, value, metadata);
			return new Future<>(validatorResult);
		}
		
		Future<ValidatorResult> future = new Future<>();
		ValidatorUtil.fetchAbsentValue(validationContext, entity, propertyName) //
				.andThen(result -> {
					if (metadata instanceof MaxLength || metadata instanceof MinLength)
						evaluateCollectionCount(propertyDisplayName, validatorResult, value, metadata);
					else
						evaluateCollectionElements(propertyDisplayName, validatorResult, value, metadata);
					future.onSuccess(validatorResult);
				}).onError(e -> {
					System.err.println("error in validateManipulations within " + this.getClass().getSimpleName());
					future.onFailure(e);
				});
		
		return future;
	}
	
	private boolean isCollectionProperty(Property property) {
		if (property.getType() == null || !property.getType().isCollection())
			return false;
		
		return true;
	}
	
	private boolean isElementLengthMetadataRespected(T metadata, int length) {
		if (metadata instanceof ElementMaxLength)
			return length <= ((ElementMaxLength) metadata).getLength();
		
		return length >= ((ElementMinLength) metadata).getLength();
	}
	
	private boolean isLengthMetadataRespected(T metadata, int length, boolean emptyOK) {
		if (metadata instanceof MaxLength)
			return length <= ((MaxLength) metadata).getLength() || (emptyOK && length == 0);
		
		return length >= ((MinLength) metadata).getLength() || (emptyOK && length == 0);
	}
	
	private void evaluateCollectionElements(String propertyDisplayName, ValidatorResult validatorResult, Object value, T metadata) {
		if (value == null || !(value instanceof Collection) || !(value instanceof Map)) {
			validatorResult.setResult(true);
			return;
		}
		
		boolean collectionIsEmpty = false;
		if (value instanceof Collection)
			collectionIsEmpty = ((Collection<?>) value).isEmpty();
		else
			collectionIsEmpty = ((Map<?,?>) value).isEmpty();
		
		int lengthInt;
		if (metadata instanceof ElementMaxLength) {
			lengthInt = ((Long) ((ElementMaxLength) metadata).getLength()).intValue();
			validatorResult.setResultMessage(LocalizedText.INSTANCE.collectionElementMaxLengthMessage(propertyDisplayName, lengthInt));
		} else {
			lengthInt = ((Long) ((ElementMinLength) metadata).getLength()).intValue();
			validatorResult.setResultMessage(LocalizedText.INSTANCE.collectionElementMinLengthMessage(propertyDisplayName, lengthInt));
		}
		
		if (collectionIsEmpty) {
			validatorResult.setResult(true);
			return;
		}
		
		String listFailComparisonValues = "";
		boolean result = true;
		List<?> elementsList;
		if (value instanceof List)
			elementsList = (List<?>) value;
		else if (value instanceof Set)
			elementsList = new ArrayList<>((Collection<?>) value);
		else
			elementsList = new ArrayList<>(((Map<?,?>) value).entrySet());
		
		for (Object element : elementsList) {
			if (!(element instanceof String))
				break;
			
			int elementLength = ((String) element).length();
			if (!isElementLengthMetadataRespected(metadata, elementLength)) {
				//validation fail
				if (!listFailComparisonValues.isEmpty())
					listFailComparisonValues = listFailComparisonValues + ", ";
				listFailComparisonValues = listFailComparisonValues + (String) element; 
				result = false;
			} 
		}

		String resultMessage = null;
		if (result) {
			if (metadata instanceof ElementMaxLength) {
				resultMessage = LocalizedText.INSTANCE.collectionElementMaxLengthFailMessage(propertyDisplayName, lengthInt,
						listFailComparisonValues);
			} else {
				resultMessage = LocalizedText.INSTANCE.collectionElementMinLengthFailMessage(propertyDisplayName, lengthInt,
						listFailComparisonValues);
			}
		}

		if (resultMessage != null)
			validatorResult.setResultMessage(resultMessage);
		validatorResult.setResult(result);
	}
	
	private String getStringResultMessage(ValidationContext validationContext, T metadata, String propertyDisplayName) {
		if (metadata instanceof MaxLength) {
			int maxInt = ((Long) ((MaxLength) metadata).getLength()).intValue();
			if (validationContext.isShortMessageStyle())
				return LocalizedText.INSTANCE.stringLesserEqualLength(String.valueOf(maxInt));				
			else	
				return LocalizedText.INSTANCE.stringMinSizeMessage(propertyDisplayName, maxInt);
		}
		
		int minInt = ((Long) ((MinLength) metadata).getLength()).intValue();
		if (validationContext.isShortMessageStyle())
			return LocalizedText.INSTANCE.stringGreatherEqualLength(String.valueOf(minInt));				
		else	
			return LocalizedText.INSTANCE.stringMaxSizeMessage(propertyDisplayName, minInt);
	}
	
	private ValidatorMessageType getValidatorMessageType(T metadata) {
		if (metadata instanceof MaxLength)
			return ValidatorMessageType.lessEqualLength;
		
		return ValidatorMessageType.greatherEqualLength;
	}
	
	private int getLength(T metadata) {
		if (metadata instanceof MaxLength)
			return ((Long) ((MaxLength) metadata).getLength()).intValue();
		
		return ((Long) ((MinLength) metadata).getLength()).intValue();
	}
	
	private void evaluateCollectionCount(String propertyDisplayName, ValidatorResult validatorResult, Object value, T metadata) {
		int collectionSize = 0;
		if (value != null) {
			if (value instanceof Collection)
				collectionSize = ((Collection<?>) value).size();
			else
				collectionSize = ((Map<?,?>) value).size();
		}
		
		validatorResult.setResultMessage(getCollectionCountMessage(metadata, propertyDisplayName));
		validatorResult.setResult(isLengthMetadataRespected(metadata, collectionSize, false));
	}
	
	private String getCollectionCountMessage(T metadata, String propertyDisplayName) {
		int length = getLength(metadata);
		if (metadata instanceof MaxLength)
			return LocalizedText.INSTANCE.collectionElementMaxCountMessage(propertyDisplayName, getLength(metadata));
		
		return LocalizedText.INSTANCE.collectionElementMinCountMessage(propertyDisplayName, length);
	}

}
