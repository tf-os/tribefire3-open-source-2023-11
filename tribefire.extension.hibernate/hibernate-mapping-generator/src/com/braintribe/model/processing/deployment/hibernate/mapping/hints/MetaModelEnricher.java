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
package com.braintribe.model.processing.deployment.hibernate.mapping.hints;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.PropertyMetaData;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;

public class MetaModelEnricher {

	private final HbmXmlGenerationContext context;

	private static final Logger log = Logger.getLogger(MetaModelEnricher.class);

	public MetaModelEnricher(HbmXmlGenerationContext context) {
		this.context = context;
	}

	public void enrich() {
		Map<String, MetaData> metaDataHints = context.getEntityHintProvider().getMetaDataHints();

		if (isEmpty(metaDataHints)) {
			log.debug(() -> "No meta data hint found, gm meta model won't be enriched");
			return;
		}

		Map<String, GmEntityTypeMappings> hintsMap = createMappingHintsMap(metaDataHints);
		if (isEmpty(hintsMap)) {
			log.debug(() -> "No valid meta data hint found, gm meta model won't be enriched");
			return;
		}

		GmMetaModel clonedGmMetaModel = cloneGmMetaModel(context.getGmMetaModel());

		int typesToEnrich = hintsMap.size();
		int enrichedTypes = 0;

		for (GmType gmType: clonedGmMetaModel.getTypes()) {
			if (!gmType.isGmEntity())
				continue;

			GmEntityType gmEntityType = (GmEntityType) gmType;
			
			GmEntityTypeMappings mappingHint = hintsMap.get(gmEntityType.getTypeSignature());
			if (mappingHint != null) {
				boolean applied = applyMetaData(gmEntityType, mappingHint);
				if (applied)
					enrichedTypes++;
			}

			if (enrichedTypes >= typesToEnrich)
				break;
		}
		
		if (enrichedTypes > 0)
			context.overwriteGmMetaModel(clonedGmMetaModel);
	}

	private static GmMetaModel cloneGmMetaModel(GmMetaModel gmMetaModel) {
		EntityType<GmMetaModel> entityType = gmMetaModel.entityType();

		GmMetaModel clonedGmMetaModel = (GmMetaModel) entityType.clone(gmMetaModel, null, null);

		return clonedGmMetaModel;
	}

	private static boolean applyMetaData(GmEntityType gmEntityType, GmEntityTypeMappings mappingHint) {

		boolean applied = false;

		if (mappingHint.entityMapping != null) {
			if (gmEntityType.getMetaData() == null) {
				log.warn("null metadata set returned for " + gmEntityType.getTypeSignature());
			} else {
				gmEntityType.getMetaData().add(cloneMetaData(mappingHint.entityMapping));
				applied = true;
			}
		}

		if (gmEntityType.getProperties() != null && mappingHint.propertyMappings != null && !mappingHint.propertyMappings.isEmpty()) {

			for (GmProperty gmProperty : gmEntityType.getProperties()) {

				PropertyMetaData propertyMetaData = mappingHint.propertyMappings.get(gmProperty.getName());

				if (propertyMetaData != null) {
					if (gmProperty.getMetaData() == null) {
						log.warn("null metadata set returned for property " + gmEntityType.getTypeSignature() + "#" + gmProperty.getName());
					} else {
						gmProperty.getMetaData().add(cloneMetaData(propertyMetaData));
						applied = true;
					}
				}

			}
		}

		return applied;
	}
	
	private static <T extends MetaData> T cloneMetaData(T metaData) {
		T result = metaData.entityType().clone(new MetaDataCloningContext(), metaData, null);
		return result;
	}

	private static class MetaDataCloningContext extends StandardCloningContext {
		@Override
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
			return entityType.create();
		}

		@Override
		public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property, GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
			if (property.get(instanceToBeCloned) == null)
				return false;
			else
				return super.canTransferPropertyValue(entityType, property, instanceToBeCloned, clonedInstance, sourceAbsenceInformation);
		}
	}

	private static Map<String, GmEntityTypeMappings> createMappingHintsMap(Map<String, MetaData> metaDataHints) {

		Map<String, GmEntityTypeMappings> mappingsPerEntity = new HashMap<>(metaDataHints.size());

		for (Map.Entry<String, MetaData> entry : metaDataHints.entrySet()) {

			String key = entry.getKey();
			MetaData metaData = entry.getValue();

			String typeSignature = null;
			String propertyName = null;
			int sepIx = key.indexOf('#');

			if (sepIx < 0) {
				typeSignature = key;
			} else {
				typeSignature = key.substring(0, sepIx);
				int propIx = sepIx + 1;
				if (key.length() > propIx) {
					propertyName = key.substring(propIx);
				}
			}

			if (propertyName == null) {

				if (!(metaData instanceof EntityTypeMetaData)) {
					if (log.isWarnEnabled()) {
						log.warn("Unexpected type of metadata associated with key [" + key + "]: " + metaData);
					}
					continue;
				}

				GmEntityTypeMappings gmEntityTypeMappings = mappingsPerEntity.get(typeSignature);
				if (gmEntityTypeMappings == null) {
					gmEntityTypeMappings = new GmEntityTypeMappings();
					mappingsPerEntity.put(typeSignature, gmEntityTypeMappings);
				}

				gmEntityTypeMappings.entityMapping = (EntityTypeMetaData) metaData;

			} else {

				if (!(metaData instanceof PropertyMetaData)) {
					if (log.isWarnEnabled()) {
						log.warn("Unexpected type of metadata associated with key [" + key + "]: " + metaData);
					}
					continue;
				}

				GmEntityTypeMappings gmEntityTypeMappings = mappingsPerEntity.get(typeSignature);
				if (gmEntityTypeMappings == null) {
					gmEntityTypeMappings = new GmEntityTypeMappings();
					mappingsPerEntity.put(typeSignature, gmEntityTypeMappings);
				}

				gmEntityTypeMappings.propertyMappings.put(propertyName, (PropertyMetaData) metaData);

			}

		}

		return mappingsPerEntity;

	}

	/**
	 * <p>
	 * Groups {@link EntityMapping} and {@link PropertyMapping} per type signature.
	 */
	private static class GmEntityTypeMappings {
		private EntityTypeMetaData entityMapping;
		private final Map<String, PropertyMetaData> propertyMappings = new HashMap<>();

	}

}
