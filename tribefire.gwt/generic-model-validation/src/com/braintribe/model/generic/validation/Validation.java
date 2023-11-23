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
package com.braintribe.model.generic.validation;

import java.util.List;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.validation.expert.Predator;
import com.braintribe.model.generic.validation.expert.ValidationContext;
import com.braintribe.model.generic.validation.log.ValidationLog;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class Validation {
	
	private boolean alreadyTriggered = false;
	private Predator predator;
	private ValidationLog validationLog;
	private PersistenceGmSession gmSession;
	
	@Configurable
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	@Required
	public void setPredator(Predator predator) {
		this.predator = predator;
	}

	public Predator getPredator() {
		return predator;
	}
	
	public ValidationLog getValidationLog() {
		return validationLog;
	}
	
	public void setValidationLog(ValidationLog validationLog) {
		this.validationLog = validationLog;
	}
	
	public Future<ValidationLog> validateManipulations() {
		return validateManipulations(gmSession.getTransaction().getManipulationsDone());
	}

	public Future<ValidationLog> validateManipulations(List<Manipulation> manipulations) {
		ValidationContext validationContext = new ValidationContext();
		validationContext.setGmSession(gmSession);
		validationContext.setGroupValidations(true);
		validationContext.setShortMessageStyle(true);
		return predator.validateManipulations(validationContext, manipulations);
	}
	
	public Future<ValidationLog> validateManipulation(Manipulation manipulation) {
		ValidationContext validationContext = new ValidationContext();
		validationContext.setGmSession(gmSession);
		validationContext.setGroupValidations(true);
		validationContext.setShortMessageStyle(true);
		return predator.validateManipulation(validationContext,manipulation);
	}
	
	public Future<ValidationLog> validateEntity(GenericEntity entity) {
		ValidationContext validationContext = new ValidationContext();
		validationContext.setGmSession((PersistenceGmSession) entity.session());
		validationContext.setGroupValidations(true);
		validationContext.setShortMessageStyle(true);
		return predator.validateEntity(entity, validationContext);
	}

	public Future<ValidationLog> validateEntityWithContext(GenericEntity entity, ValidationContext validationContext) {
		if (validationContext.getGmSession() == null)
			validationContext.setGmSession((PersistenceGmSession) entity.session());
		return predator.validateEntity(entity, validationContext);
	}
	
	/*
	public Future<ValidationLog> configuredEntityValidation(GenericEntity entity) {
		ValidationContext validationContext = new ValidationContext();
		validationContext.setGmSession((PersistenceGmSession) entity.session());
		validationContext.setCompleteConfiguredValidation(true);
		validationContext.setGroupValidations(true);
		validationContext.setShortMessageStyle(true);
		return predator.validateEntity(entity, validationContext);
	}
	*/
	
	public Future<ValidationLog> validatePropertyValue(GenericEntity entity, String propertyName, Object propertyValue) {
		ValidationContext validationContext = new ValidationContext();
		validationContext.setGmSession((PersistenceGmSession) entity.session());
		validationContext.setGroupValidations(true);
		validationContext.setShortMessageStyle(true);
		return predator.validatePropertyValue(entity, validationContext, propertyName, propertyValue);
	}

	public Future<ValidationLog> configuredPropertyValidation(GenericEntity entity, String propertyName) {
		ValidationContext validationContext = new ValidationContext();
		validationContext.setGmSession((PersistenceGmSession) entity.session());
		validationContext.setCompleteConfiguredValidation(true);
		validationContext.setGroupValidations(true);
		validationContext.setShortMessageStyle(true);
		return predator.validatePropertyValue(entity, validationContext, propertyName, null);
	}	
	
	public boolean isAlreadyTriggered() {
		return this.alreadyTriggered;
	}
	
	public void reset() {
		this.alreadyTriggered = false;
	}
	
	public void setAlreadyTriggered(boolean alreadyTriggered) {
		this.alreadyTriggered = alreadyTriggered;
	}
}
