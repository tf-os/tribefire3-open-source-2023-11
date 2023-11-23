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
package com.braintribe.model.library.service;

import com.braintribe.model.accessapi.AccessDataRequest;
import com.braintribe.model.generic.annotation.meta.Confidential;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

public interface SetupLibraryEnvironment extends LibraryBaseRequest, AccessDataRequest {

	final EntityType<SetupLibraryEnvironment> T = EntityTypes.T(SetupLibraryEnvironment.class);
	
	public final static String environment = "environment";

	void setEnvironment(Environment environment);
	Environment getEnvironment();
	
	void setDatabasePassword(String databasePassword);
	@Confidential
	String getDatabasePassword();
	
	@Override
	EvalContext<? extends SetupLibraryEnvironmentResult> eval(Evaluator<ServiceRequest> evaluator);

}
