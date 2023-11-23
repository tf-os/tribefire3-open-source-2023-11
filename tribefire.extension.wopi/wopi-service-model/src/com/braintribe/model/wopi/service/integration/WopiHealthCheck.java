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

import static tribefire.extension.wopi.model.WopiMetaDataConstants.NUMBER_OF_CHECKS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.NUMBER_OF_CHECKS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SIMPLE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SIMPLE_NAME;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Max;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Health check - opens all supported WOPI documents
 * 
 *
 */
public interface WopiHealthCheck extends WopiRequest {

	EntityType<WopiHealthCheck> T = EntityTypes.T(WopiHealthCheck.class);

	@Override
	EvalContext<? extends WopiHealthCheckResult> eval(Evaluator<ServiceRequest> evaluator);

	String simple = "simple";
	String prepareDocuments = "prepareDocuments";
	String numberOfChecks = "numberOfChecks";

	@Name(SIMPLE_NAME)
	@Description(SIMPLE_DESCRIPTION)
	@Mandatory
	@Initializer("true")
	boolean getSimple();
	void setSimple(boolean simple);

	@Name(NUMBER_OF_CHECKS_NAME)
	@Description(NUMBER_OF_CHECKS_DESCRIPTION)
	@Initializer("1")
	@Min("1")
	@Max("50")
	int getNumberOfChecks();
	void setNumberOfChecks(int numberOfChecks);

}