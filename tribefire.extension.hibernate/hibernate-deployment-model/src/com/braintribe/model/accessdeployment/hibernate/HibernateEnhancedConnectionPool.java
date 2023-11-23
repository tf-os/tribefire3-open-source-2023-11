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

import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A {@link DatabaseConnectionPool} (i.e. {@link javax.sql.DataSource}) which delegates all it's method to the data source of the configured
 * {@link #getHibernateComponent() hibernate component}. However, on very first usage it ensures the DB schema using the sessionFactory of its
 * Hibernate component.
 */
public interface HibernateEnhancedConnectionPool extends DatabaseConnectionPool {

	EntityType<HibernateEnhancedConnectionPool> T = EntityTypes.T(HibernateEnhancedConnectionPool.class);

	String hibernateComponent = "hibernateComponent";

	@Mandatory
	HibernateComponent getHibernateComponent();
	void setHibernateComponent(HibernateComponent hibernateComponent);

}
