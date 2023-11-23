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

import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.validation.ValidatorResult;
import com.braintribe.model.generic.validation.util.ValidatorUtil;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.OrderingDirection;

public class UniqueKeyValidator extends AbstractValidator<Unique> {
	
	@Override
	public Future<ValidatorResult> validateValue(ValidationContext validationContext, GenericEntity entity, Unique metadata,
			EntitySignatureAndPropertyName entitySignatureAndPropertyName, Object value) {
		String propertyName = entitySignatureAndPropertyName.getPropertyName();
		String displayInfo = ValidatorUtil.getPropertyDisplayName(getGmSession(), entity, propertyName);
		
		ValidatorResult validatorResult = ValidatorUtil.prepareValidatorResult(propertyName);
		if (!metadata.isTrue() || value == null) {
			validatorResult.setResult(true);
			return new Future<>(validatorResult);
		}
		
		EntityQueryBuilder entityQueryBuilder = EntityQueryBuilder//
				.from(entitySignatureAndPropertyName.getEntityTypeSignature())//
				.orderBy(propertyName, OrderingDirection.ascending);
		Object entityId = entity.getId();
		if (entityId == null) {
			entityQueryBuilder = entityQueryBuilder.where()//
					.property(propertyName).eq(value.toString());
		} else {
			entityQueryBuilder = entityQueryBuilder.where()
					.conjunction()
						.property(propertyName).eq(value.toString())
						.property(GenericEntity.id).ne(entityId)
					.close();
		}
		EntityQuery entityQuery = entityQueryBuilder.done();
		
		Future<ValidatorResult> future = new Future<>();
		validationContext.getGmSession().query().entities(entityQuery).result(com.braintribe.processing.async.api.AsyncCallback.of(conv -> {
			try {
				EntityQueryResult result = conv.result();
				boolean valueFound = false;
				Object entityValue = entity.entityType().getProperty(propertyName).get(entity);
				Object entityIdValue = entity.getId();
				EntityType<GenericEntity> metaDataEntityType = GMF.getTypeReflection()
						.getType(entitySignatureAndPropertyName.getEntityTypeSignature());
				for (GenericEntity localChangedEntity : validationContext.getChangedEntitiesByEntityType(metaDataEntityType)) {
					Object localChangedEnityValue = metaDataEntityType.getProperty(propertyName).get(localChangedEntity);
					Object localChangedEntityIdValue = localChangedEntity.getId();
					if (entityIdValue != null && localChangedEntityIdValue != null && !localChangedEntityIdValue.equals(entityIdValue)) {
						valueFound = entityValue.equals(localChangedEnityValue);
						break;
					} else if (entity != localChangedEntity) {
						valueFound = entityValue.equals(localChangedEnityValue);
						break;
					}
				}
				
				String message = displayInfo + ": '" + value + "' " + LocalizedText.INSTANCE.uniqueMessage();
				validatorResult.setResultMessage(message);
				validatorResult.setResult(result.getEntities().isEmpty() && !valueFound);
				future.onSuccess(validatorResult);
			} catch (GmSessionException e) {
				future.onFailure(e);
			}
		}, future::onFailure));
		
		return future;
	}

}
