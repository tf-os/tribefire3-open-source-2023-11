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
package tribefire.extension.opentracing;

/**
 *
 */
public interface OpentracingConstants {

	String MODULE_GROUPID = "tribefire.extension.opentracing";

	String MODULE_EXTERNALID = MODULE_GROUPID + ":opentracing-module";

	String MODULE_GLOBAL_ID = "module://" + MODULE_EXTERNALID;

	String DEPLOYMENT_MODEL_QUALIFIEDNAME = MODULE_GROUPID + ":opentracing-deployment-model";

	String DATA_MODEL_QUALIFIEDNAME = MODULE_GROUPID + ":opentracing-data-model";

	String SERVICE_MODEL_QUALIFIEDNAME = MODULE_GROUPID + ":opentracing-service-model";

	String OPENTRACING_WORKBENCH_ACCESS_MODEL = MODULE_GROUPID + ":opentracing-access-workbench-model";

	String OPENTRACING_WORKBENCH_ACCESS_EXTERNALID = "extension.opentracing.access.wb";
	String OPENTRACING_WORKBENCH_ACCESS_GLOBALID = OPENTRACING_WORKBENCH_ACCESS_EXTERNALID;

	String ACCESS_ID_CORTEX = "cortex";

	String MAJOR_VERSION = "1";
}
