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

import java.util.Set;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.wopi.WopiSession;

/**
 * Export a WOPI session
 * 
 *
 */
public interface ExportWopiSession extends WopiRequest {

	EntityType<ExportWopiSession> T = EntityTypes.T(ExportWopiSession.class);

	@Override
	EvalContext<? extends ExportWopiSessionResult> eval(Evaluator<ServiceRequest> evaluator);

	String wopiSessions = "wopiSessions";
	String includeDiagnosticPackage = "includeDiagnosticPackage";
	String includeCurrentResource = "includeCurrentResource";
	String includeResourceVersions = "includeResourceVersions";
	String includePostOpenResourceVersions = "includePostOpenResourceVersions";

	@Mandatory
	Set<WopiSession> getWopiSessions();
	void setWopiSessions(Set<WopiSession> wopiSessions);

	@Initializer("false")
	@Mandatory
	boolean getIncludeDiagnosticPackage();
	void setIncludeDiagnosticPackage(boolean includeDiagnosticPackage);

	@Initializer("true")
	@Mandatory
	boolean getIncludeCurrentResource();
	void setIncludeCurrentResource(boolean includeCurrentResource);

	@Initializer("true")
	@Mandatory
	boolean getIncludeResourceVersions();
	void setIncludeResourceVersions(boolean includeResourceVersions);

	@Initializer("true")
	@Mandatory
	boolean getIncludePostOpenResourceVersions();
	void setIncludePostOpenResourceVersions(boolean includePostOpenResourceVersions);

}
