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
package tribefire.module.wire.contract;

import javax.sql.DataSource;

import com.braintribe.model.deployment.database.pool.DatabaseConnectionInfoProvider;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.processing.deployment.api.ComponentBinder;

import tribefire.cortex.model.deployment.mimetypedetection.MimeTypeDetector;

/**
 * Aggregation of all {@link ComponentBinder}s available on the {@link TribefireWebPlatformContract web platform}.
 */
public interface WebPlatformBindersContract extends //
		AccessBindersContract, //
		CheckBindersContract, //
		ClusterBindersContract, //
		MarshallingBindersContract, //
		ResourceProcessingBindersContract, //
		ServiceBindersContract, //
		ServletBindersContract, //
		StateProcessingBindersContract, //
		WorkerBindersContract {

	ComponentBinder<MimeTypeDetector, com.braintribe.mimetype.MimeTypeDetector> mimeTypeDetector();

	ComponentBinder<DatabaseConnectionPool, DataSource> databaseConnectionPool();

	ComponentBinder<DatabaseConnectionInfoProvider, tribefire.module.api.DatabaseConnectionInfoProvider> databaseConnectionInfoProvider();

}
