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

import static tribefire.extension.wopi.model.WopiMetaDataConstants.DOCUMENT_MODE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DOCUMENT_MODE_NAME;

import java.util.Set;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.wopi.DocumentMode;

/**
 * Add TEST document
 * 
 *
 */
public interface EnsureTestDoc extends WopiRequest {

	EntityType<EnsureTestDoc> T = EntityTypes.T(EnsureTestDoc.class);

	@Override
	EvalContext<? extends EnsureTestDocResult> eval(Evaluator<ServiceRequest> evaluator);

	String documentMode = "documentMode";
	String testNames = "testNames";

	@Name(DOCUMENT_MODE_NAME)
	@Description(DOCUMENT_MODE_DESCRIPTION)
	@Initializer("enum(com.braintribe.model.wopi.DocumentMode,view)")
	@Mandatory
	DocumentMode getDocumentMode();
	void setDocumentMode(DocumentMode documentMode);

	// TODO: add name/description
	Set<String> getTestNames();
	void setTestNames(Set<String> testNames);

}