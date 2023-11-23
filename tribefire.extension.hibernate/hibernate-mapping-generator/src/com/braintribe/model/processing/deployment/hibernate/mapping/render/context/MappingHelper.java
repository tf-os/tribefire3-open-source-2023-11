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
package com.braintribe.model.processing.deployment.hibernate.mapping.render.context;

import static com.braintribe.utils.lcd.StringTools.isEmpty;

import java.util.Optional;

import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.accessdeployment.jpa.meta.JpaEmbeddable;
import com.braintribe.model.accessdeployment.jpa.meta.JpaPropertyMapping;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;
import com.braintribe.model.processing.deployment.hibernate.mapping.hints.EntityHint;
import com.braintribe.model.processing.deployment.hibernate.mapping.hints.PropertyHint;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;

public class MappingHelper {

	public static boolean mapEntityToDb(GmEntityType gmEntityType, EntityHint entityHint, MappingMetaDataResolver mappingMetaDataResolver) {
		boolean entityHintMapToDb = entityHint == null ? true : Boolean.TRUE.equals(entityHint.mapToDb);

		EntityMapping entityMapping = mappingMetaDataResolver.getEntityMapping(gmEntityType);
		boolean entityMappingMapToDb = entityMapping == null ? true : Boolean.TRUE.equals(entityMapping.getMapToDb());

		return entityHintMapToDb && entityMappingMapToDb;
	}

	public static boolean mapPropertyToDb(GmEntityType gmEntityType, GmProperty gmProperty, EntityHint entityHint,
			MappingMetaDataResolver mappingMdResolver) {

		boolean propertyHintMapToDb = optionalPropertyHint(entityHint, gmProperty.getName()) //
				.map(propertyHint -> Boolean.TRUE.equals(propertyHint.mapToDb)) //
				.orElse(true);

		boolean propertyMappingMapToDb = Optional.ofNullable(mappingMdResolver.getPropertyMapping(gmEntityType, gmProperty)) //
				.map(JpaPropertyMapping::isMapped) //
				.orElse(true);

		return propertyHintMapToDb && propertyMappingMapToDb;
	}

	public static Optional<PropertyHint> optionalPropertyHint(EntityHint entityHint, String propertyName) {
		return Optional.ofNullable(entityHint) //
				.map(EntityHint::getProperties) //
				.map(properties -> properties.get(propertyName));
	}

	public static boolean isForcedMappingEntity(GmEntityType gmEntityType, MappingMetaDataResolver mappingMetaDataResolver) {
		EntityMapping entityMapping = mappingMetaDataResolver.getEntityMapping(gmEntityType);
		return entityMapping != null && entityMapping.getForceMapping();
	}

	public static boolean isXmlSnippet(JpaPropertyMapping mapping) {
		if (mapping instanceof PropertyMapping) {
			PropertyMapping pm = (PropertyMapping) mapping;
			return !isEmpty(pm.getXml()) || !isEmpty(pm.getXmlFileUrl());
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	public static boolean isEmbeddable(GmEntityType gmEntityType, HbmXmlGenerationContext context) {
		EntityMdResolver entityMdResolver = context.entityMd(gmEntityType);
		if (entityMdResolver.is(JpaEmbeddable.T))
			return true;

		EntityMapping em = entityMdResolver.meta(EntityMapping.T).exclusive();
		return em != null && em.getIsEmbeddable();
	}

	public static JpaPropertyMapping resolveJpaPropertyMapping(HbmXmlGenerationContext context, GmEntityType gmEntityType, GmProperty gmProperty) {
		return context.getMappingMetaDataResolver().getPropertyMapping(gmEntityType, gmProperty);
	}

}
