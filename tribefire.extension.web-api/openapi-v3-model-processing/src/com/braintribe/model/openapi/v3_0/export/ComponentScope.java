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
package com.braintribe.model.openapi.v3_0.export;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.DdraEndpoint;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.openapi.v3_0.OpenApi;
import com.braintribe.model.openapi.v3_0.OpenapiComponents;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * This class describes a resolving scope for {@link OpenapiComponents}. A scope has a certain {@link GmMetaModel} which
 * is used as a base to resolve the {@link OpenapiComponents}. These components are reusable and can be shared between
 * {@link OpenapiContext}s and even {@link OpenApi} documents. An {@link OpenApi} document is created with components
 * from multiple scopes because
 * <li>A general scope is used to cache common components
 * <li>{@link OpenApi} documents are resolved from {@link GenericEntity}s from different models. I.e. the generic query
 * {@link DdraEndpoint} parameters are modeled independently from the requests of a specific service model.
 *
 * @author Neidhart.Orlich
 *
 */
public class ComponentScope {
	public static final String USECASE_DDRA = "ddra";
	public static final String USECASE_OPENAPI = "openapi";
	public static final String USECASE_INCLUDE_SESSION_ID = USECASE_OPENAPI + ":include-session-id";

	private Map<String, String> shortToFullRefKey = new HashMap<>();
	private final GmMetaModel model;
	private final ModelOracle modelOracle;
	private final CmdResolver cmdResolver;
	private final OpenapiComponents components;

	public ComponentScope(ComponentScope components, CmdResolver cmdResolver) {
		this(components.components, cmdResolver);
		this.shortToFullRefKey = components.shortToFullRefKey;
	}
	
	public ComponentScope(OpenapiComponents components, CmdResolver cmdResolver) {
		this.cmdResolver = cmdResolver;
		this.modelOracle = cmdResolver.getModelOracle();
		this.model = modelOracle.getGmMetaModel();

		this.components = components;
	}
	
	public void transferFrom(ComponentScope otherScope) {
		OpenapiComponents otherComponents = otherScope.components;
		
		components.getSchemas().putAll(otherComponents.getSchemas());
		components.getHeaders().putAll(otherComponents.getHeaders());
		components.getResponses().putAll(otherComponents.getResponses());
		components.getParameters().putAll(otherComponents.getParameters());
		
		if (shortToFullRefKey.isEmpty()) {
			shortToFullRefKey.putAll(otherScope.shortToFullRefKey);
		} else {
			otherScope.shortToFullRefKey.entrySet().forEach(e -> 
				shortToFullRefKey.merge(e.getKey(), e.getValue(), (k,v) -> {return handleReAssignment(k, v, e.getValue());})
			);
		}
	}

	private String handleReAssignment(String k, String v, String newValue) {
		if (v.equals(newValue))
			return v;
		
		throw new IllegalStateException("Short refKey '" + k + "' was already assigned to full key '" + v + "' but re-assignment to '" + newValue + "' was attempted");
	}

	public OpenapiComponents components() {
		return components;
	}

	public GmMetaModel getModel() {
		return model;
	}

	public ModelOracle getModelOracle() {
		return modelOracle;
	}

	public CmdResolver getCmdResolver() {
		return cmdResolver;
	}

	public String getFullRefKey(String shortRefKey) {
		return shortToFullRefKey.get(shortRefKey);
	}
	
	public void registerShortRefKey(String shortRefKey, String fullRefKey) {
		shortToFullRefKey.put(shortRefKey, fullRefKey);
	}
}
