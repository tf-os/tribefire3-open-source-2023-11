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

import static tribefire.extension.wopi.model.WopiMetaDataConstants.USER_SESSION_ID_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.USER_SESSION_ID_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_SESSION_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_SESSION_NAME;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.wopi.WopiSession;

/**
 * Open WOPI document
 * 
 *
 */
public interface OpenWopiDocument extends WopiRequest {

	EntityType<OpenWopiDocument> T = EntityTypes.T(OpenWopiDocument.class);

	@Override
	EvalContext<? extends OpenWopiDocumentResult> eval(Evaluator<ServiceRequest> evaluator);

	String wopiSession = "wopiSession";
	String userSessionId = "userSessionId";

	@Name(WOPI_SESSION_NAME)
	@Description(WOPI_SESSION_DESCRIPTION)
	@Mandatory
	WopiSession getWopiSession();
	void setWopiSession(WopiSession wopiSession);

	@Name(USER_SESSION_ID_NAME)
	@Description(USER_SESSION_ID_DESCRIPTION)
	String getUserSessionId();
	void setUserSessionId(String userSessionId);

}