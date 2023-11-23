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

import static tribefire.extension.wopi.model.WopiMetaDataConstants.ONLY_MAIN_TYPES_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.ONLY_MAIN_TYPES_NAME;

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
 * Add DEMO documents - they include all supported formats
 * 
 *
 */
public interface AddDemoDocs extends WopiRequest {

	EntityType<AddDemoDocs> T = EntityTypes.T(AddDemoDocs.class);

	@Override
	EvalContext<? extends AddDemoDocsResult> eval(Evaluator<ServiceRequest> evaluator);

	String onlyMainTypes = "onlyMainTypes";

	@Name(ONLY_MAIN_TYPES_NAME)
	@Description(ONLY_MAIN_TYPES_DESCRIPTION)
	@Mandatory
	@Initializer("true")
	boolean getOnlyMainTypes();
	void setOnlyMainTypes(boolean onlyMainTypes);

}