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
package com.braintribe.gwt.gme.websocket.client;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.service.api.ServiceRequest;

public class WebSocketHandlerRegistry implements Function<EntityType<? extends ServiceRequest>, WebSocketHandler<?>> {
	
	private Map<EntityType<? extends ServiceRequest>, Supplier<? extends WebSocketHandler<?>>> webSocketHandlers;
	
	@Required
	public void setWebSocketHandlers(Map<EntityType<? extends ServiceRequest>, Supplier<? extends WebSocketHandler<?>>> webSocketHandlers) {
		this.webSocketHandlers = webSocketHandlers;
	}

	@Override
	public WebSocketHandler<?> apply(EntityType<? extends ServiceRequest> type) {
		Supplier<? extends WebSocketHandler<?>> supplier = webSocketHandlers.get(type);
		return supplier == null ? null : supplier.get();
	}

}
