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

import static tribefire.extension.wopi.model.WopiMetaDataConstants.SOURCE_REFERENCE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SOURCE_REFERENCE_NAME;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.MaxLength;
import com.braintribe.model.generic.annotation.meta.MinLength;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Unmodifiable;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Finds a WOPI session by source reference
 * 
 *
 */
public interface FindWopiSessionBySourceReference extends WopiRequest {

	EntityType<FindWopiSessionBySourceReference> T = EntityTypes.T(FindWopiSessionBySourceReference.class);

	@Override
	EvalContext<? extends FindWopiSessionBySourceReferenceResult> eval(Evaluator<ServiceRequest> evaluator);

	String sourceReference = "sourceReference";

	@Name(SOURCE_REFERENCE_NAME)
	@Description(SOURCE_REFERENCE_DESCRIPTION)
	@Unmodifiable
	@Mandatory
	@MinLength(1)
	@MaxLength(255)
	String getSourceReference();
	void setSourceReference(String sourceReference);

}
