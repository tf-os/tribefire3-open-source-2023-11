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
package com.braintribe.model.processing.email.util;

/**
 *
 */
public interface EmailConstants {

	// -----------------------------------------------------------------------
	// MISC
	// -----------------------------------------------------------------------

	String GROUP_ID = "com.braintribe.model.email";

	String GLOBAL_ID_SERVICE_PROCESSOR_PREFIX = "tribefire:serviceProcessor/";
	String GLOBAL_ID_EMAIL_PROFILE_PREFIX = "tribefire:emailProfile/";
	String GLOBAL_ID_CONNECTION_PREFIX = "tribefire:connection/";
	String GLOBAL_ID_ACCESS_PREFIX = "tribefire:access/";

	// -----------------------------------------------------------------------
	// MODELS
	// -----------------------------------------------------------------------

	String EMAIL_MODEL_NAME = "email-model";
	String EMAIL_MODEL_QUALIFIEDNAME = GROUP_ID + ":" + EMAIL_MODEL_NAME;

	String EMAIL_DEPLOYMENT_MODEL_NAME = "email-deployment-model";
	String EMAIL_DEPLOYMENT_MODEL_QUALIFIEDNAME = GROUP_ID + ":" + EMAIL_DEPLOYMENT_MODEL_NAME;

	String EMAIL_SERVICE_MODEL_NAME = "email-service-model";
	String EMAIL_SERVICE_MODEL_QUALIFIEDNAME = GROUP_ID + ":" + EMAIL_SERVICE_MODEL_NAME;

	// -----------------------------------------------------------------------
	// EXTERNAL IDs
	// -----------------------------------------------------------------------

	String EXTERNAL_ID_EMAIL_SERVICE_PROCESSOR = "email.serviceProcessor";
	String EXTERNAL_ID_EMAIL_HEALTHCHECK_PROCESSOR = "email.healthCheckProcessor";

}
