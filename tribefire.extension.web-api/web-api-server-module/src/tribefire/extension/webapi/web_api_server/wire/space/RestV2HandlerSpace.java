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
package tribefire.extension.webapi.web_api_server.wire.space;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.ddra.endpoints.v2.RestV2Endpoint;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.AbstractRestV2Handler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2DeleteEntitiesHandler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2DeletePropertiesHandler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2GetEntitiesHandler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2GetPropertiesHandler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2Handler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2OptionsHandler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2PatchEntitiesHandler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2PatchPropertiesHandler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2PostEntitiesHandler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2PostPropertiesHandler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2PutEntitiesHandler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2PutPropertiesHandler;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class RestV2HandlerSpace implements WireSpace{
	
	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Import
	private TcSpace tc;
	
	@Managed
	public Map<String, RestV2Handler<RestV2Endpoint>> handlers() {
		Map<String, RestV2Handler<?>> handlers = new HashMap<>();

		// entities
		handlers.put("GET:entities", getEntities());
		handlers.put("POST:entities", postEntities());
		handlers.put("PUT:entities", putEntities());
		handlers.put("PATCH:entities", patchEntities());
		handlers.put("DELETE:entities", deleteEntities());
		handlers.put("OPTIONS:entities", optionsHandler());

		// properties
		handlers.put("GET:properties", getProperties());
		handlers.put("POST:properties", postProperties());
		handlers.put("PUT:properties", putProperties());
		handlers.put("PATCH:properties", patchProperties());
		handlers.put("DELETE:properties", deleteProperties());
		handlers.put("OPTIONS:properties", optionsHandler());

		return (Map<String, RestV2Handler<RestV2Endpoint>>) (Map<?, ?>) handlers;
	}

	@Managed
	private RestV2GetEntitiesHandler getEntities() {
		RestV2GetEntitiesHandler handler = new RestV2GetEntitiesHandler();
		handler.setModelAccessoryFactory(tfPlatform.requestUserRelated().modelAccessoryFactory());
		configureHandlerCommons(handler);
		return handler;
	}

	@Managed
	private RestV2PostEntitiesHandler postEntities() {
		RestV2PostEntitiesHandler handler = new RestV2PostEntitiesHandler();
		handler.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		configureHandlerCommons(handler);
		return handler;
	}

	@Managed
	private RestV2PutEntitiesHandler putEntities() {
		RestV2PutEntitiesHandler handler = new RestV2PutEntitiesHandler();
		handler.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		configureHandlerCommons(handler);
		return handler;
	}

	@Managed
	private RestV2PatchEntitiesHandler patchEntities() {
		RestV2PatchEntitiesHandler handler = new RestV2PatchEntitiesHandler();
		handler.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		configureHandlerCommons(handler);
		return handler;
	}

	@Managed
	private RestV2DeleteEntitiesHandler deleteEntities() {
		RestV2DeleteEntitiesHandler handler = new RestV2DeleteEntitiesHandler();
		handler.setModelAccessoryFactory(tfPlatform.requestUserRelated().modelAccessoryFactory());
		configureHandlerCommons(handler);
		return handler;
	}

	@Managed
	private RestV2OptionsHandler optionsHandler() {
		RestV2OptionsHandler handler = new RestV2OptionsHandler();
		return handler;
	}

	@Managed
	private RestV2GetPropertiesHandler getProperties() {
		RestV2GetPropertiesHandler handler = new RestV2GetPropertiesHandler();
		configureHandlerCommons(handler);
		return handler;
	}

	@Managed
	private RestV2PostPropertiesHandler postProperties() {
		RestV2PostPropertiesHandler handler = new RestV2PostPropertiesHandler();
		configureHandlerCommons(handler);
		return handler;
	}

	@Managed
	private RestV2PutPropertiesHandler putProperties() {
		RestV2PutPropertiesHandler handler = new RestV2PutPropertiesHandler();
		configureHandlerCommons(handler);
		return handler;
	}

	@Managed
	private RestV2PatchPropertiesHandler patchProperties() {
		RestV2PatchPropertiesHandler handler = new RestV2PatchPropertiesHandler();
		configureHandlerCommons(handler);
		return handler;
	}

	@Managed
	private RestV2DeletePropertiesHandler deleteProperties() {
		RestV2DeletePropertiesHandler handler = new RestV2DeletePropertiesHandler();
		configureHandlerCommons(handler);
		return handler;
	}
	
	private void configureHandlerCommons(AbstractRestV2Handler<?> handler) {
		handler.setEvaluator(tfPlatform.requestUserRelated().evaluator());
		handler.setMarshallerRegistry(tfPlatform.marshalling().registry());
		handler.setTraversingCriteriaMap(tc.criteriaMap());
	}

}
