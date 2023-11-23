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
package tribefire.extension.wopi.initializer.wire.contract;

import com.braintribe.model.accessdeployment.hibernate.HibernateDialect;
import com.braintribe.wire.api.annotation.Decrypt;
import com.braintribe.wire.api.annotation.Default;

import tribefire.cortex.initializer.support.wire.contract.PropertyLookupContract;
import tribefire.extension.wopi.processing.EntityStorageType;
import tribefire.extension.wopi.processing.StorageType;

public interface RuntimePropertiesContract extends PropertyLookupContract {

	@Default("false")
	boolean WOPI_CREATE_DEFAULT();

	// -----------------------------------------------------------------------
	// WopiTemplateContext
	// -----------------------------------------------------------------------

	@Default("default")
	String WOPI_CONTEXT();

	String WOPI_STORAGE_FOLDER();

	@Default("fs")
	StorageType WOPI_STORAGE_TYPE();

	@Default("smood")
	EntityStorageType WOPI_ENTITY_STORAGE_TYPE();

	// -----------------------------------------------------------------------
	// WopiTemplateDatabaseContext
	// -----------------------------------------------------------------------

	@Default("org.postgresql.Driver")
	String WOPI_DB_DRIVER();

	@Default("jdbc:postgresql://localhost:5432/cortex")
	String WOPI_DB_URL();

	@Default("cortex")
	String WOPI_DB_USER();

	@Decrypt
	@Default("Ck6fxSjtrititl7cOQ7+C75K6LWN+40spCAJeZFkL6K+mYc+le+ERFM4uZ7e4FxLldgBUg==") // cortex
	String WOPI_DB_PASSWORD();

	HibernateDialect WOPI_DB_HIBERNATEDIALECT();

	@Default("WOPI_")
	String WOPI_TABLE_PREFIX();

	@Default("2")
	int WOPI_DB_MIN_POOL_SIZE();
	@Default("50")
	int WOPI_DB_MAX_POOL_SIZE();
}
