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
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.validation.ValidatorResult;
import com.braintribe.model.meta.data.MetaData;

// PGA TODO FIX
public class CompoundUniqueKeyValidator extends Validator<MetaData/* CompoundUniqueKeyConstraint*/> {
	
	//private Future<ValidatorResult> validatorResultFuture;
	//private ValidatorResult validatorResult;

	@Override
	public Future<ValidatorResult> validate(final ValidationContext validationContext, final GenericEntity entity, final MetaData /*CompoundUniqueKeyConstraint*/ metaData,
			final EntitySignatureAndPropertyName entitySignatureAndPropertyName) {

		throw new UnsupportedOperationException("Method 'CompoundUniqueKeyValidator.validate' is not implemented yet!");
		
//		validatorResult = new ValidatorResult();
//		validatorResult.setMetaData(metaData);
//		String displayInfo = "";
//		final EntityType<GenericEntity> entityType = entity.entityType();
//		
//		for (String propertyName : metaData.getUniqueProperties()) {
//			PropertyDisplayInfo basicDisplayInfo = getGmSession().getModelAccessory().getMetaData().entityType(entityType)
//					.property(entitySignatureAndPropertyName.getPropertyName()).meta(PropertyDisplayInfo.T).exclusive();			 
//			if (basicDisplayInfo != null) {
//				displayInfo += "'" + I18nTools.getDefault(basicDisplayInfo.getName(), "") + "', ";
//			} else
//				displayInfo += "'" + propertyName + "', ";
//		}	
//		validatorResult.setPropertyName(displayInfo);
//		
//		if (!metaData.getUnique()) {
//			validatorResult.setResult(true);
//			return new Future<ValidatorResult>(validatorResult);
//		}
//		
//		final EntityQuery entityQuery = EntityQuery.T.create();
//		SimpleOrdering ordering = SimpleOrdering.T.create();
//		ordering.setDirection(OrderingDirection.ascending);
//		ordering.setOrderBy(ValidatorUtil.propertyOperand(entityType.getIdProperty().getPropertyName()));//entitySignatureAndPropertyName.getPropertyName());
//		Restriction restriction = Restriction.T.create();
//		Conjunction conjunction = Conjunction.T.create();
//		List<Condition> operands = new ArrayList<>();
//		
//		String values = "";
//		for (String propertyName : metaData.getUniqueProperties()) {
//			ValueComparison valueComparison = ValueComparison.T.create();
//			valueComparison.setLeftOperand(ValidatorUtil.propertyOperand(propertyName));
//			valueComparison.setOperator(Operator.equal);
//			Object propertyValue = entityType.getProperty(propertyName).getProperty(entity);
//			/*EntityType<Value> valueType = GMF.getTypeReflection().getEntityType(ValueUtil.getValueType(propertyValue));
//			Value value = valueType.newInstance();
//			valueType.setPropertyValue(value, ValueUtil.VALUE_PROPERTY_NAME, ValueUtil.wrapValue(propertyValue));*/
//			valueComparison.setRightOperand(propertyValue);
//			operands.add(valueComparison);
//			values += "'" + propertyValue.toString() + "', ";
//		}
//		
//		if (entity.persistenceId() != null) {
//			ValueComparison idComparison = ValueComparison.T.create();
//			idComparison.setLeftOperand(ValidatorUtil.propertyOperand(entityType.getIdProperty().getPropertyName()));
//			idComparison.setOperator(Operator.notEqual);		
//			Object id = entity.persistenceId();
//			/*EntityType<Value> valueType = GMF.getTypeReflection().getEntityType(ValueUtil.getValueType(entityType.getIdProperty().getPropertyType().getJavaType()));				
//			Value idValue = valueType.newInstance();
//			valueType.setPropertyValue(idValue, ValueUtil.VALUE_PROPERTY_NAME, id);*/
//			idComparison.setRightOperand(id);				
//			operands.add(idComparison);				
//		}
//		
//		conjunction.setOperands(operands);
//		restriction.setCondition(conjunction);
//
//		entityQuery.setOrdering(ordering);
//		entityQuery.setRestriction(restriction);
//		entityQuery.setEntityTypeSignature(entitySignatureAndPropertyName.getEntityTypeSignature());
//		
//		validatorResultFuture = new Future<ValidatorResult>() {
//			@Override
//			public void get(AsyncCallback<? super ValidatorResult> asyncCallback) {
//				super.get(asyncCallback);
//				queryEntities(this, validationContext, entityQuery);
//			}
//			
//			private void queryEntities(final Future<ValidatorResult> future, final ValidationContext validationContext, EntityQuery entityQuery) {
//				validationContext.getGmSession().query().entities(entityQuery).result(new com.braintribe.processing.async.api.AsyncCallback<EntityQueryResultConvenience>() {						
//					@Override
//					public void onSuccess(EntityQueryResultConvenience conv) {
//						EntityQueryResult result;
//						try {
//							result = conv.result();
//							boolean valueFound = false;
//							for (String gmProperty : metaData.getUniqueProperties()) {
//								Object entityValue = entityType.getProperty(gmProperty).getProperty(entity);
//								Object entityIdValue = entity.persistenceId();
//								EntityType<GenericEntity> metaDataEntityType = GMF.getTypeReflection().getType(entitySignatureAndPropertyName.getEntityTypeSignature());
//								for (GenericEntity localChangedEntity : validationContext.getChangedEntitiesByEntityType(metaDataEntityType)) {								
//									Object localChangedEntityValue = metaDataEntityType.getProperty(gmProperty).getProperty(localChangedEntity);
//									Object localChangedEntityIdValue = localChangedEntity.persistenceId();
//									if ((entityIdValue != null && localChangedEntityIdValue != null) && !localChangedEntityIdValue.equals(entityIdValue)) {
//										valueFound = valueFound & entityValue.equals(localChangedEntityValue);
//										break;
//									} else if (entity != localChangedEntity) {
//										valueFound = valueFound & entityValue.equals(localChangedEntityValue);
//										break;
//									}
//								}
//							}
//							
//							validatorResult.setResult(result.getEntities().size() == 0 && !valueFound);
//							future.onSuccess(validatorResult);
//						} catch (GmSessionException e) {
//							e.printStackTrace();
//						}							
//					}
//					
//					@Override
//					public void onFailure(Throwable t) {
//						System.err.println("error while validateManipulations within UniqeKeyValidator");
//						future.onFailure(t);
//					}
//				});
//			}
//		};				
//		
//		validatorResult.setResultMessage(LocalizedText.INSTANCE.compoundUniqueMessage(values));			
//		return validatorResultFuture;
	}

	@Override
	public Future<ValidatorResult> validateValue(ValidationContext validationContext, GenericEntity entity,
			MetaData metaData, EntitySignatureAndPropertyName entitySignatureAndPropertyName, Object value) {
		throw new UnsupportedOperationException("Method 'CompoundUniqueKeyValidator.validate' is not implemented yet!");
	}

}
