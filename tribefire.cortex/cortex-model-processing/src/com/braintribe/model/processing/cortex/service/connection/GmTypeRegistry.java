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
package com.braintribe.model.processing.cortex.service.connection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.dbs.DbColumn;
import com.braintribe.model.dbs.DbTable;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

/**
* 
*/
class GmTypeRegistry {

	private static GmMetaModel metaModel;
	private final Map<String, GmEntityType> entities = new HashMap<>();
	private final Map<String, GmProperty> properties = new HashMap<>();
	private final Map<String, GmSimpleType> simpleTypes = new HashMap<>();
	private final Map<GmEntityType, DbTable> entityToTableMapping = new HashMap<>();
	private final Map<GmProperty, DbColumn> propertyToColumnMapping = new HashMap<>();
	
	public GmTypeRegistry(GmMetaModel metaModel, PersistenceGmSession session) throws GmSessionException {
		GmTypeRegistry.metaModel = metaModel;
		for (GmType gmType: metaModel.getTypes()) {
			if (gmType.isGmEntity()) {
				registerEntity((GmEntityType) gmType);
			}
		}
		
		EntityQuery query = EntityQueryBuilder.from(GmSimpleType.class).done();
		List<GmSimpleType> gmSimpleTypes = session.query().entities(query).list();
		
		for (GmSimpleType gmSimpleType : gmSimpleTypes) {
			simpleTypes.put(gmSimpleType.getTypeSignature(), gmSimpleType);
		}
	}

	public GmSimpleType getGmSimpleType(String signature) {
		return simpleTypes.get(signature);
	}

	public GmEntityType getGmEntityType(String signature) {
		return entities.get(signature);
	}

	public void registerTableForType(GmEntityType type, DbTable table) {
		this.entityToTableMapping.put(type, table);
	}

	public void registerColumnForProperty(GmProperty property, DbColumn column) {
		this.propertyToColumnMapping.put(property, column);
	}

	public void registerEntity(GmEntityType gmEntityType) {
		entities.put(gmEntityType.getTypeSignature(), gmEntityType);
		
		if (gmEntityType.getProperties() == null) {
			return;
		}
		for (GmProperty gmProperty : gmEntityType.getProperties()) {
			registerProperty(gmProperty);
		}
		
	}

	public GmProperty getProperty(DbColumn dbColumn) {
		return properties.get(getPropertySignature(dbColumn));
	}

	public void registerProperty(GmProperty gmProperty) {
		properties.put(getPropertySignature(gmProperty), gmProperty);
	}

	
	public Map<GmEntityType, DbTable> getEntityTableMappings() {
		return entityToTableMapping;
	}
	
	public DbColumn getColumnForProperty(GmProperty property) {
		return propertyToColumnMapping.get(property);
	}
	
	
	private static String getEntitySignatureFrom(DbTable dbTable) {
		return DbTableProcessingUtils.getEntitySignatureFrom(metaModel, dbTable);
	}

	private static String getPropertySignature(GmProperty gmProperty) {
		return gmProperty.getDeclaringType().getTypeSignature() + "#" + gmProperty.getName();
	}

	private static String getPropertySignature(DbColumn dbColumn) {
		return getEntitySignatureFrom(dbColumn.getOwner()) + "#" + getPropertyNameFrom(dbColumn);
	}

	private static String getPropertyNameFrom(DbColumn dbColumn) {
		return DbTableProcessingUtils.getPropertyName(dbColumn);
	}

	
	
}
