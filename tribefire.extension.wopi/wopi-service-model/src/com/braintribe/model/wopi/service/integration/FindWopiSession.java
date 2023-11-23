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
package com.braintribe.model.wopi.service.integration;

import static tribefire.extension.wopi.model.WopiMetaDataConstants.CORRELATIONID_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CORRELATIONID_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.FIND_CLOSED_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.FIND_CLOSED_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.FIND_EXPIRED_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.FIND_EXPIRED_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.INCLUDE_CURRENT_RESOURCE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.INCLUDE_CURRENT_RESOURCE_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.INCLUDE_POST_OPEN_RESOURCE_VERSIONS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.INCLUDE_POST_OPEN_RESOURCE_VERSIONS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.INCLUDE_RESOURCE_VERSIONS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.INCLUDE_RESOURCE_VERSIONS_NAME;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Finds a WOPI session
 * 
 *
 */
public interface FindWopiSession extends WopiRequest {

	EntityType<FindWopiSession> T = EntityTypes.T(FindWopiSession.class);

	@Override
	EvalContext<? extends FindWopiSessionResult> eval(Evaluator<ServiceRequest> evaluator);

	String correlationId = "correlationId";
	String findClosed = "findClosed";
	String findExpired = "findExpired";
	String includeCurrentResource = "includeCurrentResource";
	String includeResourceVersions = "includeResourceVersions";
	String includePostOpenResourceVersions = "includePostOpenResourceVersions";

	@Name(CORRELATIONID_NAME)
	@Description(CORRELATIONID_DESCRIPTION)
	@Mandatory
	String getCorrelationId();
	void setCorrelationId(String correlationId);

	@Name(FIND_CLOSED_NAME)
	@Description(FIND_CLOSED_DESCRIPTION)
	@Initializer("false")
	boolean getFindClosed();
	void setFindClosed(boolean findClosed);

	@Name(FIND_EXPIRED_NAME)
	@Description(FIND_EXPIRED_DESCRIPTION)
	@Initializer("false")
	boolean getFindExpired();
	void setFindExpired(boolean findExpired);

	@Name(INCLUDE_CURRENT_RESOURCE_NAME)
	@Description(INCLUDE_CURRENT_RESOURCE_DESCRIPTION)
	@Mandatory
	@Initializer("false")
	boolean getIncludeCurrentResource();
	void setIncludeCurrentResource(boolean includeCurrentResource);

	@Name(INCLUDE_RESOURCE_VERSIONS_NAME)
	@Description(INCLUDE_RESOURCE_VERSIONS_DESCRIPTION)
	@Mandatory
	@Initializer("false")
	boolean getIncludeResourceVersions();
	void setIncludeResourceVersions(boolean includeResourceVersions);

	@Name(INCLUDE_POST_OPEN_RESOURCE_VERSIONS_NAME)
	@Description(INCLUDE_POST_OPEN_RESOURCE_VERSIONS_DESCRIPTION)
	@Mandatory
	@Initializer("false")
	boolean getIncludePostOpenResourceVersions();
	void setIncludePostOpenResourceVersions(boolean includePostOpenResourceVersions);

}
