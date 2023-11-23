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
package tribrefire.extension.okta.common;

public interface OktaCommons {

	String OKTA_EXTENSION_GROUP_ID = "tribefire.extension.okta";

	String OKTA_DATA_MODEL_NAME = OKTA_EXTENSION_GROUP_ID + ":okta-model";
	String OKTA_API_MODEL_NAME = OKTA_EXTENSION_GROUP_ID + ":okta-api-model";
	String OKTA_DEPLOYMENT_MODEL_NAME = OKTA_EXTENSION_GROUP_ID + ":okta-deployment-model";

	String MODEL_GLOBAL_ID_PREFIX = "model:";
	String WIRE_GLOBAL_ID_PREFIX = "wire://";
	String OKTA_MODEL_GLOBAL_ID_PREFIX = MODEL_GLOBAL_ID_PREFIX + OKTA_EXTENSION_GROUP_ID + ":";

	String OKTA_DEPLOYMENT_MODEL_GLOBAL_ID = OKTA_MODEL_GLOBAL_ID_PREFIX + "okta-deployment-model";
	String OKTA_API_MODEL_GLOBAL_ID = OKTA_MODEL_GLOBAL_ID_PREFIX + "okta-api-model";
	String OKTA_MODEL_GLOBAL_ID = OKTA_MODEL_GLOBAL_ID_PREFIX + "okta-model";

	String OKTA_HTTP_AUTHORIZATION_SCHEME = "SSWS";
	String OKTA_HTTP_PARAM_AUTHORIZATION = "Authorization";
	String OKTA_HTTP_PATH_USERS = "/users";
	String OKTA_HTTP_PATH_USER = "/users/{userId}";
	String OKTA_HTTP_PATH_USER_GROUPS = "/users/{userId}/groups";
	String OKTA_HTTP_PATH_GROUPS = "/groups";
	String OKTA_HTTP_PATH_LIST_GROUP_MEMBERS = "/groups/{groupId}/users";
	String OKTA_HTTP_PATH_GROUP = "/groups/{groupId}";
	String OKTA_HTTP_PATH_LIST_APP_USERS = "/apps/{appId}/users";
	String OKTA_HTTP_PATH_LIST_APP_GROUPS = "/apps/{appId}/groups";

	String OKTA_HTTP_PATH_GET_OAUTH_ACCESS_TOKEN = "/../../oauth2/v1/token";

	String DEFAULT_OKTA_ACCESS_EXTERNALID = "access.okta";
	String DEFAULT_OKTA_ACCESS_NAME = "Okta Access";

	String OKTA_HTTP_PROCESSOR_EXTERNALID = "processor.http.okta";
	String OKTA_HTTP_PROCESSOR_NAME = "Okta Http Processor";

	String OKTA_HTTP_CONNECTOR_EXTERNALID = "connector.http.okta";
	String OKTA_HTTP_CONNECTOR_NAME = "Okta Http Client";

	String OKTA_AUTHORIZATION_PREPROCESSOR_EXTERNALID = "processor.okta.pre.authorization";
	String OKTA_AUTHORIZATION_PREPROCESSOR_NAME = "Okta Authorization Pre Processor";

	String OKTA_AUTHORIZATION_PREPROCESSOR_CLIENTSECRET_EXTERNALID = "processor.okta.pre.authorization.clientsecret";
	String OKTA_AUTHORIZATION_PREPROCESSOR_CLIENTSECRET_NAME = "Okta Authorization Pre Processor (with Client Secret)";

}
