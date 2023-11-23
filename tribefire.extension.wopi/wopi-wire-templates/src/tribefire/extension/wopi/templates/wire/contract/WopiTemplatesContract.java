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
package tribefire.extension.wopi.templates.wire.contract;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.deployment.resource.filesystem.FileSystemBinaryProcessor;
import com.braintribe.model.deployment.resource.sql.SqlBinaryProcessor;
import com.braintribe.model.wopi.connector.WopiWacConnector;
import com.braintribe.model.wopi.service.CleanupWopiSessionWorker;
import com.braintribe.model.wopi.service.ExpireWopiSessionWorker;
import com.braintribe.model.wopi.service.WopiApp;
import com.braintribe.model.wopi.service.WopiIntegrationExample;
import com.braintribe.model.wopi.service.WopiServiceProcessor;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.wopi.templates.api.WopiTemplateContext;

public interface WopiTemplatesContract extends WireSpace {

	/**
	 * Setup WOPI with a specified {@link WopiTemplateContext}
	 */
	void setupWopi(WopiTemplateContext context);

	IncrementalAccess access(WopiTemplateContext context);

	WopiServiceProcessor wopiServiceProcessor(WopiTemplateContext context);

	WopiApp wopiApp(WopiTemplateContext context);

	WopiIntegrationExample wopiIntegrationExample(WopiTemplateContext context);

	WopiWacConnector wopiWacConnector(WopiTemplateContext context);

	CleanupWopiSessionWorker cleanupWopiSessionWorker(WopiTemplateContext context);

	ExpireWopiSessionWorker expireWopiSessionWorker(WopiTemplateContext context);

	IncrementalAccess storageAccess(WopiTemplateContext context);

	DatabaseConnectionPool connectionPool(WopiTemplateContext context);

	SqlBinaryProcessor sqlBinaryProcessor(WopiTemplateContext context);

	FileSystemBinaryProcessor filesystemBinaryProcessor(WopiTemplateContext context);

	CollaborativeSmoodAccess wopiWorkbenchAccess(WopiTemplateContext context);

}
