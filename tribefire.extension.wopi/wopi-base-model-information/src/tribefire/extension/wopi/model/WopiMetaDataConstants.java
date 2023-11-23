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
package tribefire.extension.wopi.model;

/**
 * 'Name' and 'Description' metadata constants to be attached to entities
 * 
 *
 */
public interface WopiMetaDataConstants {

	// -----------------------------------------------------------------------
	// LIST OF TEXT/DESCRIPTION FOR METADATA ASSIGNMENT
	// -----------------------------------------------------------------------

	// ONLY WopiWacConnector

	String WAC_DISCOVERY_ENDPOINT_NAME = "WAC Discovery Endpoint";
	String WAC_DISCOVERY_ENDPOINT_DESCRIPTION = "URL endpoint of the WOPI client";

	String CUSTOM_PUBLIC_SERVICES_URL_NAME = "Custom Public Services URL";
	String CUSTOM_PUBLIC_SERVICES_URL_DESCRIPTION = "Custom Public Services URL if the endpoint cannot be calculated automatically - this is really a rare use case! e.g. 'http://1.1.1.1:1234/tribefire-services' is a valid value";

	String CONNECTION_REQUEST_TIMEOUT_IN_MS_NAME = "Connection Request Timeout in ms";
	String CONNECTION_REQUEST_TIMEOUT_IN_MS_DESCRIPTION = "Timeout in milliseconds for connection requests";

	String CONNECT_TIMEOUT_IN_MS_NAME = "Connect Timeout in ms";
	String CONNECT_TIMEOUT_IN_MS_DESCRIPTION = "Timeout in milliseconds for connect attempts";

	String SOCKET_TIMEOUT_IN_MS_NAME = "Socket Timeout in ms";
	String SOCKET_TIMEOUT_IN_MS_DESCRIPTION = "Timeout in milliseconds for socket";

	String CONNECTION_RETRIES_NAME = "Connection Retries";
	String CONNECTION_RETRIES_DESCRIPTION = "Number of Connection Retries";

	String DELAY_ON_RETRY_IN_MS_NAME = "Delay on Retry in ms";
	String DELAY_ON_RETRY_IN_MS_DESCRIPTION = "Duration between retries";

	// ONLY WopiServiceProcessor
	String WOPI_APP_NAME = "WOPI App";
	String WOPI_APP_DESCRIPTION = "WOPI App - endpoint for REST";

	String WOPI_SERVICE_PROCESSOR_LOG_WARNING_THRESHOLD_IN_MS_NAME = "Service Processor Log WARN threshold in ms";
	String WOPI_SERVICE_PROCESSOR_LOG_WARNING_THRESHOLD_IN_MS_DESCRIPTION = "Threshold of service execution in milliseconds to log WARN message in Service Processor";

	String WOPI_SERVICE_PROCESSOR_LOG_ERROR_THRESHOLD_IN_MS_NAME = "Service Processor Log ERROR threshold in ms";
	String WOPI_SERVICE_PROCESSOR_LOG_ERROR_THRESHOLD_IN_MS_DESCRIPTION = "Threshold of service execution in milliseconds to log ERROR message in Service Processor";

	String WOPI_SERVICE_PROCESSOR_TEST_DOC_COMMAND_NAME = "";
	String WOPI_SERVICE_PROCESSOR_TEST_DOC_COMMAND_DESCRIPTION = "";

	// ONLY WopiApp

	String CONTEXT_NAME = "Context";
	String CONTEXT_DESCRIPTION = "Context information to distinguish multiple WOPI deployments";

	String WOPI_WAC_CONNECTOR_NAME = "WOPI WAC Connector";
	String WOPI_WAC_CONNECTOR_DESCRIPTION = "WAC Connector pointing to WOPI client";

	String ACCESS_NAME = "Access";
	String ACCESS_DESCRIPTION = "WOPI access holding information related to WOPI - e.g. WOPI Session";

	String LOCK_EXPIRATION_IN_MS_NAME = "Lock Expiration in ms";
	String LOCK_EXPIRATION_IN_MS_DESCRIPTION = "Expiration of Lock in milliseconds of separation of critical section";

	String WOPI_SESSION_EXPIRATION_IN_MS_NAME = "WOPI Session Expiration in ms";
	String WOPI_SESSION_EXPIRATION_IN_MS_DESCRIPTION = "Expiration of WOPI Session in milliseconds for open WOPI sessions";

	String WOPI_APP_LOG_WARNING_THRESHOLD_IN_MS_NAME = "WOPI App Log WARN threshold in ms";
	String WOPI_APP_LOG_WARNING_THRESHOLD_IN_MS_DESCRIPTION = "Threshold of service execution in milliseconds to log WARN message in WOPI App";

	String WOPI_APP_LOG_ERROR_THRESHOLD_IN_MS_NAME = "WOPI App Log ERROR threshold in ms";
	String WOPI_APP_LOG_ERROR_THRESHOLD_IN_MS_DESCRIPTION = "Threshold of service execution in milliseconds to log ERROR message in WOPI App";

	String WOPI_APP_ACCESS_TOKEN_TTL_NAME = "Access Token TTL";
	String WOPI_APP_ACCESS_TOKEN_TTL_DESCRIPTION = "TTL of Access Token in seconds";

	String APPLY_EARLY_FILE_SIZE_CHECKS_NAME = "Early File Size Checks";
	String APPLY_EARLY_FILE_SIZE_CHECKS_DESCRIPTION = "Does a file size check while creating the WopiSession";

	// ONLY WopiSession

	String VERSION_NAME = "Version";
	String VERSION_DESCRIPTION = "Version of the WopiSession";

	String WOPI_STATUS_NAME = "Status";
	String WOPI_STATUS_DESCRIPTION = "Status of the WOPI Session - either 'open', 'expired' or 'closed'";

	String RESOURCE_VERSIONS_NAME = "Resource Versions";
	String RESOURCE_VERSIONS_DESCRIPTION = "History Resources of the WOPI session - Holds intermediate verions of the document itself.";

	String POST_OPEN_RESOURCE_VERSIONS_NAME = "Post Open Resource Versions";
	String POST_OPEN_RESOURCE_VERSIONS_DESCRIPTION = "History Resources of the WOPI session after closing/expiring a session - Holds verions of the document that are already closed/expired.";

	String CREATOR_ID_NAME = "Creator ID";
	String CREATOR_ID_DESCRIPTION = "Creator ID of the WOPI Session";

	String CREATOR_NAME = "Creator";
	String CREATOR_DESCRIPTION = "Creator of the WOPI Session";

	String CREATION_DATE_NAME = "Creation Date";
	String CREATION_DATE_DESCRIPTION = "Creation Date of the WOPI session";

	String LAST_UPDATED_NAME = "Last Updated";
	String LAST_UPDATED_DESCRIPTION = "Date of last update of WOPI session";

	String LAST_UPDATED_USER_NAME = "Last Updated User";
	String LAST_UPDATED_USER_DESCRIPTION = "User of last update of WOPI session";

	String LAST_UPDATED_USER_ID_NAME = "Last Updated User ID";
	String LAST_UPDATED_USER_ID_DESCRIPTION = "User ID of last update of WOPI session";

	String WOPI_URL_NAME = "WOPI URL";
	String WOPI_URL_DESCRIPTION = "URL for accessing the document itself in the Browser";

	String LOCK_NAME = "Lock";
	String LOCK_DESCRIPTION = "WOPI Lock for concurrent operations.";

	// ONLY FindWopiSession
	String FIND_CLOSED_NAME = "Find Closed";
	String FIND_CLOSED_DESCRIPTION = "Find also closed session";

	String FIND_EXPIRED_NAME = "Find Expired";
	String FIND_EXPIRED_DESCRIPTION = "Find expired session";

	String INCLUDE_CURRENT_RESOURCE_NAME = "Include Current Resource";
	String INCLUDE_CURRENT_RESOURCE_DESCRIPTION = "Include Current Resource to be attached";

	String INCLUDE_RESOURCE_VERSIONS_NAME = "Include Resource Versions";
	String INCLUDE_RESOURCE_VERSIONS_DESCRIPTION = "Include Resource Versions to be attached";

	String INCLUDE_POST_OPEN_RESOURCE_VERSIONS_NAME = "Include Post Open Resource Versions";
	String INCLUDE_POST_OPEN_RESOURCE_VERSIONS_DESCRIPTION = "Include Post Open Resource Versions to be attached";

	// ONLY RemoveAllWopiSessions
	String FORCE_REMOVE_NAME = "Force Remove";
	String FORCE_REMOVE_DESCRIPTION = "Force to Remove all WOPI Sessions";

	// ONLY WopiHealthCheck
	String SIMPLE_NAME = "Simple";
	String SIMPLE_DESCRIPTION = "Only opens one WOPI document";

	String NUMBER_OF_CHECKS_NAME = "Number of Checks";
	String NUMBER_OF_CHECKS_DESCRIPTION = "Number of Checks for health check";

	// ONLY ExpireWopiSessionWorker
	String EXPIRE_INTERVAL_NAME = "Expiration Interval";
	String EXPIRE_INTERVAL_DESCRIPTION = "Expire Interval in milliseconds for cleanup WOPI Sessions";

	// ONLY CleanupWopiSessionWorker
	String CLEANUP_INTERVAL_NAME = "Cleanup Interval";
	String CLEANUP_INTERVAL_DESCRIPTION = "Cleanup Interval in milliseconds for cleanup WOPI Sessions";

	// ONLY OpenWopiDocument
	String USER_SESSION_ID_NAME = "User Session ID";
	String USER_SESSION_ID_DESCRIPTION = "Optional User Session ID - if not set session from the request evaluation will be used";

	// ONLY AddDemoDocs
	String ONLY_MAIN_TYPES_NAME = "Only Main Types";
	String ONLY_MAIN_TYPES_DESCRIPTION = "Use only 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'";

	String URL_NAME = "URL";
	String URL_DESCRIPTION = "URL";

	// SHARED
	String MAX_VERSIONS_NAME = "Max Versions";
	String MAX_VERSIONS_DESCRIPTION = "Maximum number of versions of resources to be kept";

	String TENANT_NAME = "Tenant";
	String TENANT_DESCRIPTION = "Tenant of WOPI session";
	String TENANT_DEFAULT = "'default'";

	String WOPI_LOCK_EXPIRATION_NAME = "WOPI Lock Expiration in ms";
	String WOPI_LOCK_EXPIRATION_DESCRIPTION = "Expiration of a WOPI lock in Milliseconds";
	// ----------------

	String WOPI_SESSION_NAME = "WOPI Session";
	String WOPI_SESSION_DESCRIPTION = "WOPI Session details";

	String RESOURCE_NAME = "Resource";
	String RESOURCE_DESCRIPTION = "Resource to be uploaded to WOPI session";

	String WOPI_TAGS_NAME = "Tags";
	String WOPI_TAGS_DESCRIPTION = "Tags to be attached to a WOPI Session";
	String UPDATE_NOTIFICATION_MESSAGE_NAME = "Update Notification Message";
	String UPDATE_NOTIFICATION_MESSAGE_DESCRIPTION = "Message to be pushed to client when an update of the current resource is performed";
	String UPDATE_NOTIFICATION_ACCESS_ID_NAME = "Update Notification AccessId";
	String UPDATE_NOTIFICATION_ACCESS_ID_DESCRIPTION = "AccessId the Notification Message should be sent";

	String CORRELATIONID_NAME = "CorrelationId";
	String CORRELATIONID_DESCRIPTION = "The unique ID representing a WOPI session.";
	String SOURCE_REFERENCE_NAME = "Source Reference";
	String SOURCE_REFERENCE_DESCRIPTION = "Reference to the source - e.g. the contentId/documentId";
	String DOCUMENT_MODE_NAME = "Document Mode";
	String DOCUMENT_MODE_DESCRIPTION = "Document Mode for opening a WOPI document - either 'view' or 'edit'.";
	String ACCESS_TOKENS_NAME = "Access Tokens";
	String ACCESS_TOKENS_DESCRIPTION = "Set of Access Tokens which allows operations on the WOPI session. Refers to WOPI locking mechanism.";
	String ALLOWED_ROLES_NAME = "Allowed Roles";
	String ALLOWED_ROLES_DESCRIPTION = "Set of Allowed TF Roles to access the WOPI session.";
	String CURRENT_RESOURCE_NAME = "Current Resource";
	String CURRENT_RESOURCE_DESCRIPTION = "Current Resource of the WOPI session - Representation of the document itself.";

	// UI customization
	String SHOW_USER_FRIENDLY_NAME_NAME = "Show 'User Fiendly Name'";
	String SHOW_BREADCRUMB_BRAND_NAME_NAME = "Show 'Breadcrumb Brand Name'";
	String BREADCRUMB_BRAND_NAME_NAME = "'Breadcrumb Brand Name'";
	String URL_BREADCRUMB_BRAND_NAME = "URL assigned to 'Breadcrumb Brand Name'";
	String SHOW_BREADCRUMB_DOC_NAME_NAME = "Show 'Breadcrumb Doc Name'";
	String BREADCRUMB_DOC_NAME_NAME = "'Breadcrumb Doc Name'";
	String SHOW_BREADCRUMB_FOLDER_NAME_NAME = "Show 'Breadcrumb Folder Name'";
	String BREADCRUMB_FOLDER_NAME_NAME = "'Breadcrumb Folder Name'";
	String URL_BREADCRUMB_FOLDER_NAME = "URL assigned to 'Breadcrumb Folder Name'";
	String DISABLE_PRINT_NAME = "Disable Print";
	String DISABLE_TRANSLATION_NAME = "Disable Translation";
}
