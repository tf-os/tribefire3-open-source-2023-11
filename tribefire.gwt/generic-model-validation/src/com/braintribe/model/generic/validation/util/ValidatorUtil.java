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
package com.braintribe.model.generic.validation.util;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.validation.ValidatorResult;
import com.braintribe.model.generic.validation.expert.ValidationContext;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.utils.i18n.I18nTools;

public class ValidatorUtil {
	
	/**
	  * Checks if the given property, for the given entity, is absent.
	  */
	public static boolean checkPropertyAbsense(GenericEntity entity, String propertyName) {
		if (!(entity instanceof EnhancedEntity))
			return false;

		Property property = entity.entityType().getProperty(propertyName);
		if (property.isAbsent(entity))
			return true;

		if (property.getType().isCollection()) {
			Object collection = property.get(entity);
			if (collection instanceof EnhancedCollection)
				return !((EnhancedCollection) collection).isLoaded();
		}
		
		return false;
	}
	
	public static Future<Object> fetchAbsentValue(ValidationContext validationContext, GenericEntity entity, String propertyName) {
		Future<Object> future = new Future<>();
		validationContext.getGmSession().query().property(PropertyQueryBuilder.forProperty(entity.reference(), propertyName).done())
				.result(AsyncCallback.of(result -> {
			future.onSuccess(result.result().getPropertyValue());
		}, future::onFailure));
		
		return future;
	}
	
	public static ValidatorResult prepareValidatorResult(String propertyName) {
		ValidatorResult validatorResult = new ValidatorResult();
		validatorResult.setPropertyName(propertyName);
		
		return validatorResult;
	}
	
	public static String getPropertyDisplayName(PersistenceGmSession gmSession, GenericEntity entity, String propertyName) {
		String propertyDisplayName = propertyName;
		Name displayName = gmSession.getModelAccessory().getMetaData().entity(entity).property(propertyName).meta(Name.T).exclusive();			 
		if (displayName != null)
			propertyDisplayName = I18nTools.getDefault(displayName.getName(), propertyName);
		
		return propertyDisplayName;
	}

}
