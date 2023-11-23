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
package tribefire.extension.audit.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.NormalizedCompoundManipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.processing.aop.api.context.AroundContext;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.service.api.aspect.RequestorAddressAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorUserNameAspect;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.StringTools;

import tribefire.extension.audit.model.ManipulationRecord;
import tribefire.extension.audit.model.ManipulationType;

public class ManipulationRecordCreator {
	private static final Logger logger = Logger.getLogger(ManipulationRecordCreator.class);

	private boolean preliminaryResolving = true;
	private Supplier<String> userNameProvider = AttributeContextValueSupplier.of(RequestorUserNameAspect.class);
	private Supplier<String> userIpAddressProvider = AttributeContextValueSupplier.of(RequestorAddressAspect.class);

	public List<ManipulationRecord> createRecords(TypeUsageInfoIndex typeUsageInfoIndex, PersistenceGmSession session,
			NormalizedCompoundManipulation appliedManipulation, Manipulation inducedManipulation,
			AroundContext<ManipulationRequest, ManipulationResponse> aspectContext) {
		List<ManipulationRecord> records = new ArrayList<>();

		EntityMdResolver mdResolver = session.getModelAccessory().getCmdResolver().getMetaData().entityType(ManipulationRecord.T);
		int valueMaxLength = getMaxLength(mdResolver, ManipulationRecord.value);
		int previousValueMaxLength = getMaxLength(mdResolver, ManipulationRecord.previousValue);

		String uuid = UUID.randomUUID().toString();

		long sequence = 0;

		try {

			Date now = new Date();

			List<AtomicManipulation> appliedManipulations = appliedManipulation != null ? appliedManipulation.inline()
					: Collections.<AtomicManipulation> emptyList();
			List<AtomicManipulation> inducedManipulations = inducedManipulation != null ? inducedManipulation.inline()
					: Collections.<AtomicManipulation> emptyList();

			EntityReferenceResolver entityReferenceResolver = new EntityReferenceResolver();

			entityReferenceResolver.registerReferenceTranslations(appliedManipulations);
			entityReferenceResolver.registerReferenceTranslations(inducedManipulations);

			String userIpAddress = null;
			try {
				userIpAddress = (this.userIpAddressProvider != null) ? this.userIpAddressProvider.get() : null;
			} catch (Exception e) {
				logger.debug("Could not get user IP address.", e);
			}

			for (AtomicManipulation manipulation : appliedManipulations) {
				EntityReference reference = (EntityReference) manipulation.manipulatedEntity();

				EntityType<?> entityType = EntityTypes.get(reference.getTypeSignature());

				if (ManipulationRecord.T == entityType && reference.referenceType() == EntityReferenceType.preliminary) {
					continue;
				}

				String instanceProperty = null;
				String typeSignature = reference.getTypeSignature();
				TypeUsageInfo typeUsageInfo = typeUsageInfoIndex.getTypeUsageInfo(typeSignature);
				TrackMode trackMode = TrackMode.NONE;
				ManipulationType manipulationType = null;
				PropertyInfo propertyInfo = null;

				boolean isPropertyManipulation = false;
				Supplier<Object> payloadExtractor = null;
				boolean isCollectionManipulation = false;

				switch (manipulation.manipulationType()) {
					case ADD:
						manipulationType = ManipulationType.added;
						isPropertyManipulation = true;
						isCollectionManipulation = true;
						payloadExtractor = () -> ((AddManipulation) manipulation).getItemsToAdd();
						break;
					case CHANGE_VALUE:
						manipulationType = ManipulationType.propertyChanged;
						isPropertyManipulation = true;
						payloadExtractor = () -> ((ChangeValueManipulation) manipulation).getNewValue();
						break;
					case CLEAR_COLLECTION:
						manipulationType = ManipulationType.collectionCleared;
						isPropertyManipulation = true;
						break;
					case REMOVE:
						manipulationType = ManipulationType.removed;
						isPropertyManipulation = true;
						isCollectionManipulation = true;
						payloadExtractor = () -> ((RemoveManipulation) manipulation).getItemsToRemove();
						break;
					case INSTANTIATION:
						manipulationType = ManipulationType.instantiated;
						break;
					case DELETE:
						manipulationType = ManipulationType.deleted;
						break;
					default:
						break;
				}

				if (isPropertyManipulation) {
					PropertyManipulation propertyManipulation = (PropertyManipulation) manipulation;
					EntityProperty entityProperty = (EntityProperty) propertyManipulation.getOwner();
					instanceProperty = entityProperty.getPropertyName();
					propertyInfo = typeUsageInfo.getPropertyInfo(instanceProperty);
					trackMode = propertyInfo.getTrackMode();
				} else {
					trackMode = typeUsageInfo.getLifeCycleTrackMode();
				}

				if (trackMode == TrackMode.NONE) {
					continue;
				}

				if (this.preliminaryResolving && reference.referenceType() == EntityReferenceType.preliminary) {
					reference = entityReferenceResolver.resolvePreliminaryEntityReference((PreliminaryEntityReference) reference);
				}

				String encodedValue = null;
				String encodedPreviousValue = null;
				boolean persistRecord = true;

				if (isPropertyManipulation) {
					// extract and adapt payload
					Object payload = payloadExtractor != null ? payloadExtractor.get() : null;

					GenericModelType inferredPayloadType = propertyInfo.getProperty().getType();

					if (isCollectionManipulation && payload != null) {
						if (propertyInfo.isSetTypeProperty()) {
							Map<?, ?> paylaodAsMap = (Map<?, ?>) payload;
							Set<Object> adaptedPayload = new HashSet<Object>(paylaodAsMap.values());
							payload = adaptedPayload;
						} else if (propertyInfo.isListTypeProperty()) {
							ListType listType = (ListType) inferredPayloadType;
							inferredPayloadType = GMF.getTypeReflection().getMapType(EssentialTypes.TYPE_INTEGER,
									listType.getCollectionElementType());
						}
					}

					Object resolvedPayload = this.resolvePreliminaryReference(entityReferenceResolver, payload);
					encodedValue = ManipulationRecordValueEncoder.encode(inferredPayloadType, resolvedPayload);

					if (trackMode == TrackMode.PRESERVE && reference.referenceType() == EntityReferenceType.persistent) {

						GenericEntity existingEntity = typeUsageInfo.getExistingEntity((PersistentEntityReference) reference);

						if (existingEntity != null) {
							Property property = propertyInfo.getProperty();
							Object previousValue = property.get(existingEntity);

							encodedPreviousValue = ManipulationRecordValueEncoder.encode(property.getType(), previousValue);
						}
					}

					if (encodedValue.equals(encodedPreviousValue)) {
						persistRecord = false;
					}
				}

				// We do not want to persist a record that shows not change whatsoever because the client sends manipulations that don't
				// do anything
				if (persistRecord) {

					ManipulationRecord record = session.create(ManipulationRecord.T);

					record.setDate(now);
					record.setUser(this.userNameProvider.get());
					record.setManipulationType(manipulationType);
					record.setInstanceType(reference.getTypeSignature());
					record.setInstanceId(ManipulationRecordValueEncoder.encode(BaseType.INSTANCE, reference.getRefId()));
					record.setInstancePartition(reference.getRefPartition());
					record.setTransactionId(uuid);
					record.setSequenceNumber(sequence++);
					record.setUserIpAddress(userIpAddress);
					record.setPreliminaryInstance(reference instanceof PreliminaryEntityReference);
					record.setAccessId(aspectContext.getSession().getAccessId());

					if (isPropertyManipulation) {
						record.setInstanceProperty(instanceProperty);
						setValue(record, encodedValue, valueMaxLength);
						setPreviousValue(record, encodedPreviousValue, previousValueMaxLength);
					}

					records.add(record);

				} // If persistRecord
			}
			if (session.getTransaction().hasManipulations())
				session.commit();

		} catch (Exception e) {
			logger.error("Error while creating auditing records", e);
		}

		return records;
	}

	private int getMaxLength(EntityMdResolver mdResolver, String property) {
		long maxLength = Optional.ofNullable(mdResolver.property(property).meta(MaxLength.T).exclusive()).map(MaxLength::getLength).orElse(-1L);
		return (int) maxLength;
	}

	private void setValue(ManipulationRecord record, String encodedValue, int maxLength) {
		if (maxLength != -1 && encodedValue.length() > maxLength) {
			record.setValue(StringTools.substringByUtf8BytesLength(encodedValue, maxLength - 3).concat("..."));
			record.setOverflowValue(encodedValue);
		} else {
			record.setValue(encodedValue);
		}
	}

	private void setPreviousValue(ManipulationRecord record, String encodedValue, int maxLength) {
		if (encodedValue == null) {
			return;
		}
		if (maxLength != -1 && encodedValue.length() > maxLength) {
			record.setPreviousValue(StringTools.substringByUtf8BytesLength(encodedValue, maxLength - 3).concat("..."));
			record.setOverflowPreviousValue(encodedValue);
		} else {
			record.setPreviousValue(encodedValue);
		}
	}

	public Object resolvePreliminaryReference(final EntityReferenceResolver entityReferenceResolver, Object payload) {
		CloningContext cloningContext = new StandardCloningContext() {
			@Override
			public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
				if (instanceToBeCloned instanceof PreliminaryEntityReference) {
					PreliminaryEntityReference prelEntityRef = (PreliminaryEntityReference) instanceToBeCloned;
					return entityReferenceResolver.resolvePreliminaryEntityReference(prelEntityRef);
				}
				return super.supplyRawClone(entityType, instanceToBeCloned);
			}
			@Override
			public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
					GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
				if (instanceToBeCloned instanceof PreliminaryEntityReference) {
					return false;
				}
				return super.canTransferPropertyValue(entityType, property, instanceToBeCloned, clonedInstance, sourceAbsenceInformation);
			}
		};
		return GMF.getTypeReflection().getBaseType().clone(cloningContext, payload, StrategyOnCriterionMatch.skip);
	}

	/* Getters and Setters */

	@Configurable
	public void setUserNameProvider(Supplier<String> userNameProvider) {
		this.userNameProvider = userNameProvider;
	}

	@Configurable
	public void setUserIpAddressProvider(Supplier<String> userIpAddressProvider) {
		this.userIpAddressProvider = userIpAddressProvider;
	}

	@Configurable
	public void setPreliminaryResolving(boolean preliminaryResolving) {
		this.preliminaryResolving = preliminaryResolving;
	}

}
