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
package com.braintribe.model.processing.cortex.service.model;

import com.braintribe.model.cortexapi.model.ValidateModel;
import com.braintribe.model.cortexapi.model.ValidateModelResponse;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.management.MetaModelValidationResult;
import com.braintribe.model.management.MetaModelValidationViolation;
import com.braintribe.model.management.violation.ViolationWithEntityType;
import com.braintribe.model.management.violation.ViolationWithEnumType;
import com.braintribe.model.management.violation.ViolationWithProperty;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.cortex.service.ServiceBase;
import com.braintribe.model.processing.management.MetaModelValidator;
import com.braintribe.model.processing.notification.api.builder.CommandBuilder;
import com.braintribe.model.processing.notification.api.builder.NotificationBuilder;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.notification.api.builder.NotificationsBuilder;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class ModelValidator extends ServiceBase {

	private ValidateModel request;
	private PersistenceGmSession session;
	
	public ModelValidator(ValidateModel request, PersistenceGmSession session) {
		this.request = request;
		this.session = session;
	}
	
	public ValidateModelResponse run()  {
		
		GmMetaModel model = request.getModel();
		
		if (model == null) {
			return createConfirmationResponse("Please provide a model to be vaildated.", Level.WARNING, ValidateModelResponse.T);
		}
		
		
		MetaModelValidator validator = new MetaModelValidator();
		MetaModelValidationResult validationResult = validator.validate(model);
		
		if (validationResult.getValid()) {
			return createResponse("Successfully validated model. No violations found!",ValidateModelResponse.T);
		} else {

			NotificationsBuilder notifications = Notifications.build();
			for (MetaModelValidationViolation violation : validationResult.getViolations()) {

				GmType violatedType = findViolatedType(session, violation);
				GmProperty violatedProperty = findViolatedProperty(session, violation);
				
				
				NotificationBuilder notificationBuilder = notifications.add();
				CommandBuilder commandBuilder = 
						notificationBuilder
										.message()
											.warn()
											.message(violation.getType().toString()+" : "+violation.getMessage())
											.details(violation.getMessage())
										.close()
										.command();

				if (violatedType != null) {
					
					commandBuilder.gotoModelPath("Show Type")
									.addElement(violatedType.getTypeSignature(), violatedType)
								  .close();
					
				} else if (violatedProperty != null) {

					commandBuilder.gotoModelPath("Show Property")
									.addElement(violatedProperty.getDeclaringType(), violatedProperty.getName())
								  .close();

				}
				
				notificationBuilder.close();
				
			}
			
			addNotifications(notifications.list());
			return createResponse("Found validation violations! Click to see further details...", Level.ERROR, ValidateModelResponse.T);
		}
		
	}
	
	private GmProperty findViolatedProperty(PersistenceGmSession session, 	MetaModelValidationViolation violation) throws GmSessionException {
		if (violation instanceof ViolationWithProperty) {
			ViolationWithProperty withProperty = (ViolationWithProperty) violation;
			GmProperty property = session.query().entities(
												EntityQueryBuilder.from(GmProperty.class)
													.where()
														.conjunction()
															.property("name").eq(withProperty.getPropertyLink().getName())
															.property("entityType.typeSignature").eq(withProperty.getPropertyLink().getEntityTypeSignature())
														.close()
													.done()).unique();
			
			return property;
		}
		return null;
	}

	private GmType findViolatedType(PersistenceGmSession session, MetaModelValidationViolation violation) throws GmSessionException {
		
		String typeSignature = null; 
		if (violation instanceof ViolationWithEntityType) {
			typeSignature = ((ViolationWithEntityType) violation).getEntityTypeLink().getTypeSignature();
		} else if (violation instanceof ViolationWithEnumType) {
			typeSignature = ((ViolationWithEnumType) violation).getEnumTypeLink().getTypeSignature();
		} else if (violation instanceof ViolationWithProperty) {
			typeSignature = ((ViolationWithProperty) violation).getPropertyLink().getEntityTypeSignature();
		}
		
		if (typeSignature != null) {
			GmType type = session.query().entities( 
										EntityQueryBuilder.from(GmType.class)
											.where()
												.property("typeSignature").eq(typeSignature)
											.done()).unique();
			return type;
			
		}
		return null;
		
	}
	
}
