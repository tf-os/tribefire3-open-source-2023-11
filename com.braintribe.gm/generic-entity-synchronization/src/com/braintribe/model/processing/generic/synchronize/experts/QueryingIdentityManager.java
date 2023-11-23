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
package com.braintribe.model.processing.generic.synchronize.experts;

import java.util.Collection;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.generic.synchronize.GenericEntitySynchronizationException;
import com.braintribe.model.processing.generic.synchronize.api.IdentityManager;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

/**
 * An abstract {@link IdentityManager} implementation that provides support for querying instances in the target session.
 */
public abstract class QueryingIdentityManager implements IdentityManager {
	
	private static final Logger logger = Logger.getLogger(QueryingIdentityManager.class);
	
	protected static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	private boolean supportNullIdentityProperty = false;
	private boolean ignoreCache = false;
	
	public QueryingIdentityManager() {}
	
	@Configurable
	public void setSupportNullIdentityProperty(boolean supportNullIdentityProperty) {
		this.supportNullIdentityProperty = supportNullIdentityProperty;
	}
	
	@Configurable
	public void setIgnoreCache(boolean ignoreCache) {
		this.ignoreCache = ignoreCache;
	}
	
	protected GenericEntity query(PersistenceGmSession session, GenericEntity instance, EntityType<? extends GenericEntity> entityType, Collection<String> properties) throws GenericEntitySynchronizationException {
		GenericEntity existing = null;
		
		switch (properties.size()) {
		case 0:
			break;
		case 1:
			existing = query(session, buildQuery(instance, entityType, properties.iterator().next()));			
			break;
		default:
			existing = query(session, buildQuery(instance, entityType, properties));	
		}
			
		return existing;
	}

	
	protected GenericEntity query(PersistenceGmSession session, EntityQuery query) {
		if (query == null) {
			// No query given. ignore.
			return null;
		}
		
		try {
			GenericEntity result = null;
			if (!ignoreCache) {
				result = session.queryCache().entities(query).unique();
			}
			if (result == null) {
				result = session.query().entities(query).unique(); 
			}
			return result;
		} catch (GmSessionException e) {
			throw new GenericEntitySynchronizationException("Error while searching for existing entity.",e);
		}
	}

	protected EntityQuery buildQuery(GenericEntity instance, EntityType<? extends GenericEntity> entityType, Collection<String> properties) {

		JunctionBuilder<EntityQueryBuilder> where = EntityQueryBuilder.from(entityType).where().conjunction();
		for (String identityProperty : properties) {
			Object propertyValue = getPropertyValue(instance, entityType, identityProperty);
			if (propertyValue == null && !supportNullIdentityProperty) {
				logger.warn("The identity property: "+identityProperty+" of source instance" + instance + "is null but null values should be ignored due to configuration. (ignoreNullValues=true).");
				return null;
			}
			where.property(identityProperty).eq(propertyValue);
		}
		return where.close().done();
	
	}

	protected EntityQuery buildQuery(GenericEntity instance, EntityType<? extends GenericEntity> entityType, String property) {
		Object propertyValue = getPropertyValue(instance, entityType, property);
		if (propertyValue == null && !supportNullIdentityProperty) {
			logger.warn("The identity property: "+property+" of source instance: " + instance +" is null but null values should be ignored due to configuration. (ignoreNullValues=true).");
			return null;
		}
		// @formatter:off
		return  EntityQueryBuilder
					.from(entityType)
					.where()
						.property(property).eq(propertyValue)
					.done();
		// @formatter:off
	}
	
	/**
	 * @return Returns the property value of the property identified by given propertyName of the given instance.
	 */
	protected Object getPropertyValue(GenericEntity instance, EntityType<? extends GenericEntity> entityType, String propertyName) {
		Property identityProperty = entityType.findProperty(propertyName);
		if (identityProperty != null) {
			return identityProperty.get(instance);
		}
		return null;
	}	
}
