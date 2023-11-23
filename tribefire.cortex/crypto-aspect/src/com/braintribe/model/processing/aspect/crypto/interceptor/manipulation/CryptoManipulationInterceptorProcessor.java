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
package com.braintribe.model.processing.aspect.crypto.interceptor.manipulation;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.crypto.Encryptor;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.aop.api.context.AroundContext;
import com.braintribe.model.processing.aspect.crypto.interceptor.AbstractCryptoInterceptorProcessor;
import com.braintribe.model.processing.aspect.crypto.interceptor.CryptoInterceptionException;
import com.braintribe.model.processing.aspect.crypto.interceptor.CryptoInterceptorConfiguration;

/**
 * <p>
 * A
 * {@link com.braintribe.model.processing.aspect.crypto.interceptor.CryptoInterceptorProcessor}
 * which performs cryptographic operations on manipulation data.
 * 
 * <p>
 * When applicable, {@link ManipulationRequest} data is encrypted, whereas
 * {@link ManipulationResponse} data is decrypted.
 * 
 */
public class CryptoManipulationInterceptorProcessor
		extends AbstractCryptoInterceptorProcessor<ManipulationRequest, ManipulationResponse> {

	private static final Logger log = Logger.getLogger(CryptoManipulationInterceptorProcessor.class);

	private Map<PropertyManipulation, Object> encryptedValues = new HashMap<>();

	private boolean overwriteNewValuesWithCloning = true;

	protected CryptoManipulationInterceptorProcessor(CryptoInterceptorConfiguration cryptoInterceptorConfiguration,
			AroundContext<ManipulationRequest, ManipulationResponse> aroundContext) {
		super(cryptoInterceptorConfiguration, aroundContext, log);
	}

	@Override
	public boolean mustProcessRequest() throws CryptoInterceptionException {

		encryptChangedValues();

		return encryptedValues != null && !encryptedValues.isEmpty();

	}

	@Override
	public ManipulationRequest processRequest() throws CryptoInterceptionException {

		if (overwriteNewValuesWithCloning) {
			ManipulationRequest manipulationRequest = cloneManipulationRequest(aroundContext.getRequest());
			return manipulationRequest;
		} else {
			return aroundContext.getRequest();
		}

	}

	@Override
	public boolean mustProcessResponse() {

		// Manipulation responses are currently not encrypted nor decrypted.

		return false;
	}

	@Override
	public ManipulationResponse processResponse(ManipulationRequest request, ManipulationResponse response) {
		return response;
	}

	protected ManipulationRequest cloneManipulationRequest(ManipulationRequest manipulationRequest)
			throws CryptoInterceptionException {

		if (manipulationRequest == null) {
			throw new CryptoInterceptionException("No manipulation request.");
		}

		long t = 0;
		try {
			if (isTraceLogEnabled) {
				t = System.currentTimeMillis();
			}

			EntityType<ManipulationRequest> manipulationRequestType = manipulationRequest.entityType();

			ManipulationRequest clonedManipulationRequest = (ManipulationRequest) manipulationRequestType.clone(
					new NewValueReplacingCloningContext(), manipulationRequest, StrategyOnCriterionMatch.reference);

			return clonedManipulationRequest;

		} catch (Exception e) {
			throw asCryptoInterceptionException("Failed to clone the manipulation request", e);
		} finally {
			if (isTraceLogEnabled) {
				t = System.currentTimeMillis() - t;
				log.trace("Cloning the manipulation request took " + t + " ms");
			}
		}

	}

	protected void encryptChangedValues() throws CryptoInterceptionException {

		Manipulation requestManipulation = aroundContext.getRequest().getManipulation();

		if (requestManipulation instanceof CompoundManipulation) {

			for (Manipulation manipulation : ((CompoundManipulation) requestManipulation)
					.getCompoundManipulationList()) {
				if (manipulation instanceof PropertyManipulation) {

					PropertyManipulation atomicManipulation = (PropertyManipulation) manipulation;

					if (isEligibleForEncryption(atomicManipulation)) {
						encryptChangedValues(atomicManipulation);
					}

				}
			}

		} else if (requestManipulation instanceof PropertyManipulation) {

			PropertyManipulation atomicManipulation = (PropertyManipulation) requestManipulation;

			if (isEligibleForEncryption(atomicManipulation)) {
				encryptChangedValues(atomicManipulation);
			}

		} else {
			if (isTraceLogEnabled) {
				log.trace("Manipulaton won't be inspected for encryptable properties due to its type: "
						+ requestManipulation);
			}
		}

	}

	protected void encryptChangedValues(PropertyManipulation manipulation) throws CryptoInterceptionException {

		Owner owner = manipulation.getOwner();

		if (owner == null) {
			throw new CryptoInterceptionException("Cannot encrypt the new value of a manipulation with no owner");
		}

		String propertyName = manipulation.getOwner().getPropertyName();

		if (propertyName == null) {
			throw new CryptoInterceptionException(
					"Cannot encrypt the new value of a manipulation with no property name on its owner");
		}

		EntityType<? extends GenericEntity> entityType = getEntityTypeFromOwner(owner);

		Encryptor encryptor = mustEncrypt(entityType, propertyName);

		if (encryptor != null) {

			switch (manipulation.manipulationType()) {
			case ADD:
				encryptChangedValues(encryptor, (AddManipulation) manipulation);
				break;
			case CHANGE_VALUE:
				encryptChangedValues(encryptor, (ChangeValueManipulation) manipulation);
				break;
			default:
				return;
			}
		}

	}

	protected void encryptChangedValues(Encryptor encryptor, ChangeValueManipulation manipulation)
			throws CryptoInterceptionException {

		if (!(manipulation.getNewValue() instanceof String)) {
			return;
		}

		try {
			String encryptedNewValue = encryptor.encrypt((String) manipulation.getNewValue()).result().asString();

			if (overwriteNewValuesWithCloning) {
				encryptedValues.put(manipulation, encryptedNewValue);
			} else {
				manipulation.setNewValue(encryptedNewValue);
			}

		} catch (Exception e) {
			throw asCryptoInterceptionException("Failed to encrypt value", e);
		}

	}

	protected void encryptChangedValues(Encryptor encryptor, AddManipulation manipulation)
			throws CryptoInterceptionException {

		try {

			Map<Object, Object> items = manipulation.getItemsToAdd();

			if (items != null) {

				Map<Object, Object> newItemsToAdd = new HashMap<Object, Object>();

				boolean encrypted = false;

				for (Map.Entry<Object, Object> entry : items.entrySet()) {

					if (entry.getValue() instanceof String) {
						String newValue = encryptor.encrypt((String) entry.getValue()).result().asString();
						newItemsToAdd.put(entry.getKey(), newValue);
						encrypted = true;
					}

				}

				if (encrypted) {
					if (overwriteNewValuesWithCloning) {
						encryptedValues.put(manipulation, newItemsToAdd);
					} else {
						manipulation.setItemsToAdd(newItemsToAdd);
					}
				}

			}

		} catch (Exception e) {
			throw asCryptoInterceptionException("Failed to encrypt value", e);
		}

	}

	private static boolean isEligibleForEncryption(PropertyManipulation atomicManipulation) {
		switch (atomicManipulation.manipulationType()) {
		case ADD:
		case CHANGE_VALUE:
			return true;
		default:
			return false;
		}
	}

	private EntityType<? extends GenericEntity> getEntityTypeFromOwner(Owner owner) throws CryptoInterceptionException {

		try {

			EntityType<? extends GenericEntity> entityType = null;

			if (owner instanceof EntityProperty) {

				EntityProperty entityProperty = (EntityProperty) owner;
				String typeSignature = entityProperty.getReference().getTypeSignature();

				entityType = GMF.getTypeReflection().getEntityType(typeSignature);

				if (entityType == null) {
					throw new CryptoInterceptionException("No entity type found for: " + typeSignature);
				}

			} else if (owner instanceof LocalEntityProperty) {

				LocalEntityProperty entityProperty = (LocalEntityProperty) owner;

				entityType = entityProperty.getEntity().entityType();

				if (entityType == null) {
					throw new CryptoInterceptionException("No entity type found for: " + entityProperty.getEntity());
				}

			} else {
				throw new CryptoInterceptionException("Unsupported type of owner: " + owner);
			}

			return entityType;

		} catch (Exception e) {
			throw asCryptoInterceptionException("Failed to obtain the entity type from manipulation owner", e);
		}

	}

	protected class NewValueReplacingCloningContext extends StandardCloningContext {

		private Map<GenericEntity, String> skipProperties = new HashMap<>();

		public NewValueReplacingCloningContext() {
		}

		@Override
		@SuppressWarnings("unchecked")
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType,
				GenericEntity instanceToBeCloned) {

			GenericEntity clonedInstance = super.supplyRawClone(entityType, instanceToBeCloned);

			if (!(clonedInstance instanceof PropertyManipulation)) {
				return clonedInstance;
			}

			Object encryptedNewValue = encryptedValues.get(instanceToBeCloned);
			if (encryptedNewValue != null) {
				if (clonedInstance instanceof ChangeValueManipulation) {
					((ChangeValueManipulation) clonedInstance).setNewValue(encryptedNewValue);
					skipProperties.put(clonedInstance, "newValue");
				} else if (clonedInstance instanceof AddManipulation) {
					((AddManipulation) clonedInstance).setItemsToAdd((Map<Object, Object>) encryptedNewValue);
					skipProperties.put(clonedInstance, "itemsToAdd");
				}
			}

			return clonedInstance;

		}

		@Override
		public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
				GenericEntity instanceToBeCloned, GenericEntity clonedInstance,
				AbsenceInformation sourceAbsenceInformation) {

			if (!(clonedInstance instanceof PropertyManipulation)) {
				return super.canTransferPropertyValue(entityType, property, instanceToBeCloned, clonedInstance,
						sourceAbsenceInformation);
			}

			String skipProp = skipProperties.get(clonedInstance);

			if (skipProp == null || !property.getName().equals(skipProp)) {
				return super.canTransferPropertyValue(entityType, property, instanceToBeCloned, clonedInstance,
						sourceAbsenceInformation);
			} else {
				return false;
			}

		}

	}

}
