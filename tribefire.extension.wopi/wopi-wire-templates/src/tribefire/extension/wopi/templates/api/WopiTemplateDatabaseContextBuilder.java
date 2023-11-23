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
package tribefire.extension.wopi.templates.api;

import com.braintribe.model.accessdeployment.hibernate.HibernateDialect;

/**
 * Builder for setting up Database Template Context (HibernateAccess)
 * 
 *
 */
public interface WopiTemplateDatabaseContextBuilder {

	WopiTemplateDatabaseContextBuilder setHibernateDialect(HibernateDialect hibernateDialect);

	WopiTemplateDatabaseContextBuilder setTablePrefix(String tablePrefix);

	WopiTemplateDatabaseContextBuilder setDatabaseDriver(String databaseDriver);

	WopiTemplateDatabaseContextBuilder setDatabaseUrl(String databaseUrl);

	WopiTemplateDatabaseContextBuilder setDatabaseUsername(String databaseUsername);

	WopiTemplateDatabaseContextBuilder setDatabasePassword(String databasePassword);

	WopiTemplateDatabaseContextBuilder setMinPoolSize(Integer minPoolSize);

	WopiTemplateDatabaseContextBuilder setMaxPoolSize(Integer maxPoolSize);

	WopiTemplateDatabaseContext build();
}