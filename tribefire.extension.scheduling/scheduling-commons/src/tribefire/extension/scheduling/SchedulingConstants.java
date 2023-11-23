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
package tribefire.extension.scheduling;

public interface SchedulingConstants {

	String GROUPID = "tribefire.extension.scheduling";

	String MODULE_EXTERNALID = GROUPID + ".scheduling-module";

	String MODULE_GLOBAL_ID = "module://" + GROUPID + ":scheduling-module";

	String DEPLOYMENT_MODEL_QUALIFIEDNAME = GROUPID + ":scheduling-deployment-model";
	String SERVICE_MODEL_QUALIFIEDNAME = GROUPID + ":scheduling-api-model";
	String DATA_MODEL_QUALIFIEDNAME = GROUPID + ":scheduling-model";

	String MODEL_GLOBAL_ID_PREFIX = "model:";
	String WIRE_GLOBAL_ID_PREFIX = "wire://";
	String SCHEDULING_MODEL_GLOBAL_ID_PREFIX = MODEL_GLOBAL_ID_PREFIX + GROUPID + ":";

	String SCHEDULING_DEPLOYMENT_MODEL_GLOBAL_ID = SCHEDULING_MODEL_GLOBAL_ID_PREFIX + "scheduling-deployment-model";
	String SCHEDULING_API_MODEL_GLOBAL_ID = SCHEDULING_MODEL_GLOBAL_ID_PREFIX + "scheduling-api-model";
	String SCHEDULING_MODEL_GLOBAL_ID = SCHEDULING_MODEL_GLOBAL_ID_PREFIX + "scheduling-model";

	String ACCESS_ID = "access.scheduling";
	String WB_ACCESS_ID = "access.scheduling.wb";

	int MAJOR_VERSION = 1;

	String EXTERNAL_ID_SCHEDULING_SERVICE_PROCESSOR = "scheduling.serviceProcessor";

}
