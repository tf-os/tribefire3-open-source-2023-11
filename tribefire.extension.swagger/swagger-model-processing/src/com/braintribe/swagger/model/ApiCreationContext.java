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
package com.braintribe.swagger.model;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.swaggerapi.SwaggerRequest;

public class ApiCreationContext {

	public ModelOracle oracle;
	public ModelMdResolver mdResolver;
	public IncrementalAccess access;
	public SwaggerRequest request;
	public boolean useFullyQualifiedDefinitionName;

	public ApiCreationContext(SwaggerRequest request, ModelOracle oracle, ModelMdResolver mdResolver, IncrementalAccess access,
			boolean useFullyQualifiedDefinitionName) {
		this.oracle = oracle;
		this.mdResolver = mdResolver;
		this.access = access;
		this.request = request;
		this.useFullyQualifiedDefinitionName = useFullyQualifiedDefinitionName;
	}

}
