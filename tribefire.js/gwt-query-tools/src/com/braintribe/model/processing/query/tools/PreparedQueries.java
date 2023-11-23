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
package com.braintribe.model.processing.query.tools;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.SelectQuery;

/**
 * @author peter.gazdik
 */
public interface PreparedQueries {

	static SelectQuery modelByName(String modelName) {
		return entityBySimpleProperty(GmMetaModel.T, "name", modelName);
	}

	static SelectQuery typeBySignature(String typeSignature) {
		return typeBySignature(GmType.T, typeSignature);
	}

	static SelectQuery typeBySignature(EntityType<? extends GmType> modelElementType, String typeSignature) {
		return entityBySimpleProperty(modelElementType, "typeSignature", typeSignature);
	}

	static SelectQuery entityBySimpleProperty(EntityType<?> type, String propertyName, Object propertyValue) {
		return new SelectQueryBuilder().from(type, "t").where().property("t", propertyName).eq(propertyValue).done();
	}

	static SelectQuery entitiesByGlobalIds(Set<String> globalIds) {
		return entitiesByGlobalIds(GenericEntity.T, globalIds);
	}

	static SelectQuery entitiesByGlobalIds(EntityType<?> entityType, Set<String> globalIds) {
		return new SelectQueryBuilder().from(entityType, "t").where().property("t", GenericEntity.globalId).in(globalIds).done();
	}
}
