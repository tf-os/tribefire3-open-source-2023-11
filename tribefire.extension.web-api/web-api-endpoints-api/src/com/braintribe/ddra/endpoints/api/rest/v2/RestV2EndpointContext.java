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
package com.braintribe.ddra.endpoints.api.rest.v2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.ddra.endpoints.api.DdraEndpointContext;
import com.braintribe.model.ddra.endpoints.v2.DdraUrlPathParameters;
import com.braintribe.model.ddra.endpoints.v2.RestV2Endpoint;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.service.api.ServiceRequest;

public class RestV2EndpointContext<E extends RestV2Endpoint> extends DdraEndpointContext<E> {
	
	private DdraUrlPathParameters parameters;
	
	private CrudRequestTarget target;
	
	private EntityType<?> entityType;
	
	private Property property;
	
	protected Evaluator<ServiceRequest> evaluator;

	public RestV2EndpointContext(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	public DdraUrlPathParameters getParameters() {
		return parameters;
	}
	public void setParameters(DdraUrlPathParameters parameters) {
		this.parameters = parameters;
	}
	
	public void setTarget(CrudRequestTarget target) {
		this.target = target;
	}
	
	public CrudRequestTarget getTarget() {
		return target;
	}
	
	public EntityType<?> getEntityType() {
		return entityType;
	}
	
	public void setEntityType(EntityType<?> entityType) {
		this.entityType = entityType;
	}
	
	public void setProperty(Property property) {
		this.property = property;
	}
	
	public Property getProperty() {
		return property;
	}
	
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}
	
	public Evaluator<ServiceRequest> getEvaluator() {
		return this.evaluator;
	}
}
