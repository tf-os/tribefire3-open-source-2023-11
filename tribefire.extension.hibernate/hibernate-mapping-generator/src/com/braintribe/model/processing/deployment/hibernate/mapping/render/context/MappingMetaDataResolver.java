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

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
import com.braintribe.model.accessdeployment.jpa.meta.JpaPropertyMapping;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;

public class MappingMetaDataResolver {

	private final HbmXmlGenerationContext context;
	private Map<String, EntityMapping> entityMappingCache;
	private Map<String, JpaPropertyMapping> propertyMappingCache;
	private boolean enableCache = false;

	// TODO remove this stupid cache
	public MappingMetaDataResolver(HbmXmlGenerationContext context, boolean enableCache) {
		this.context = context;
		this.enableCache = enableCache;
		if (this.enableCache) {
			this.entityMappingCache = newMap();
			this.propertyMappingCache = newMap();
		}
	}

	public EntityMapping getEntityMapping(GmEntityType gmEntityType) {
		EntityMapping entityMapping = null;

		if (enableCache) {

			String typeSignature = gmEntityType.getTypeSignature();
			entityMapping = entityMappingCache.get(typeSignature);

			if (entityMapping == null && !entityMappingCache.containsKey(typeSignature)) {
				entityMapping = resolveEntityMapping(gmEntityType);
				entityMappingCache.put(typeSignature, entityMapping); // nulls also kept
			}

		} else {
			entityMapping = resolveEntityMapping(gmEntityType);
		}

		return entityMapping;
	}

	public JpaPropertyMapping getPropertyMapping(GmEntityType gmEntityType, GmProperty gmProperty) {
		if (!enableCache)
			return resolvePropertyMapping(gmEntityType, gmProperty);

		String propertySignature = gmEntityType.getTypeSignature() + ":" + gmProperty.getName();

		JpaPropertyMapping result = propertyMappingCache.get(propertySignature);

		if (result == null && !propertyMappingCache.containsKey(propertySignature)) {
			result = resolvePropertyMapping(gmEntityType, gmProperty);
			propertyMappingCache.put(propertySignature, result); // nulls also kept
		}

		return result;
	}

	private EntityMapping resolveEntityMapping(GmEntityType gmEntityType) {
		return context.entityMd(gmEntityType).meta(EntityMapping.T).exclusive();
	}

	private JpaPropertyMapping resolvePropertyMapping(GmEntityType gmEntityType, GmProperty gmProperty) {
		return context.propertyMd(gmEntityType, gmProperty).meta(JpaPropertyMapping.T).exclusive();
	}


}
