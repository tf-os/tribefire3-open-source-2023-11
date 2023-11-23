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

import static tribefire.extension.wopi.model.WopiMetaDataConstants.CONTEXT_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CONTEXT_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.FORCE_REMOVE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.FORCE_REMOVE_NAME;

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
 * Remove all WOPI sessions
 * 
 *
 */
public interface RemoveAllWopiSessions extends WopiRequest {

	EntityType<RemoveAllWopiSessions> T = EntityTypes.T(RemoveAllWopiSessions.class);

	@Override
	EvalContext<? extends RemoveAllWopiSessionsResult> eval(Evaluator<ServiceRequest> evaluator);

	String forceRemove = "forceRemove";
	String context = "context";

	@Name(FORCE_REMOVE_NAME)
	@Description(FORCE_REMOVE_DESCRIPTION)
	@Mandatory
	@Initializer("false")
	boolean getForceRemove();
	void setForceRemove(boolean forceRemove);

	@Name(CONTEXT_NAME)
	@Description(CONTEXT_DESCRIPTION)
	String getContext();
	void setContext(String context);

}
