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
package com.braintribe.model.accessdeployment.hibernate;

import java.util.Map;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.database.pool.HasDatabaseConnectionPool;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.DeployableComponent;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A {@link com.braintribe.model.deployment.DeployableComponent} which can provide a Hibernate SessionFactory.
 * <p>
 * The expert interface is com.braintribe.model.access.hibernate.HibernateComponent. 
 */
@Abstract
@DeployableComponent
public interface HibernateComponent extends Deployable, HasDatabaseConnectionPool {

	EntityType<HibernateComponent> T = EntityTypes.T(HibernateComponent.class);

	Map<String, String> getProperties();
	void setProperties(Map<String, String> value);

	String getDefaultSchema();
	void setDefaultSchema(String DefaultSchema);

	String getDefaultCatalog();
	void setDefaultCatalog(String DefaultCatalog);

	HibernateDialect getDialect();
	void setDialect(HibernateDialect hibernateDialect);

}
