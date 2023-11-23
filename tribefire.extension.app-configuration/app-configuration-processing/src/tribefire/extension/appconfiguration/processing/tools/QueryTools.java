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
package tribefire.extension.appconfiguration.processing.tools;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class QueryTools {

	private QueryTools() {
		// no instantiation required
	}

	public static <T extends GenericEntity> T getEntity(PersistenceGmSession gmSession, EntityType<T> type, String propertyName,
			Object propertyValue) {
		return gmSession.query().entities(EntityQueryBuilder.from(type).where().property(propertyName).eq(propertyValue).done()).first();
	}

	public static <T extends GenericEntity> T getFirstEntity(PersistenceGmSession gmSession, EntityType<T> type) {
		return gmSession.query().entities(EntityQueryBuilder.from(type).done()).first();
	}

	public static <T extends GenericEntity> T getEntityById(PersistenceGmSession gmSession, EntityType<T> type, Object id) {
		return gmSession.query().entities(EntityQueryBuilder.from(type).where().property(GenericEntity.id).eq(id).done()).first();
	}

}
