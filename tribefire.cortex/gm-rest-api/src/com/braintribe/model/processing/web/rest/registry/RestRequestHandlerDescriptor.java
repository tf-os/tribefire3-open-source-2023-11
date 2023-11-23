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
package com.braintribe.model.processing.web.rest.registry;

import java.util.Set;

import com.braintribe.model.processing.web.rest.RestRequestHandler;
import com.braintribe.model.rest.RequestMethod;
import com.braintribe.model.rest.RestRequest;

/**
 * Defines the structure for a {@link RestRequestHandlerRegistry} entry.
 * @param <T>
 */
public interface RestRequestHandlerDescriptor<T extends RestRequest> {

	/**
	 * Gets the REST request URL path.
	 * @return
	 */
	String getUrlPath();
	

	/**
	 * Gets a {@link Set} of {@link RequestMethod}(s) that should be supported by the given URL path.  
	 * @return
	 */
	Set<RequestMethod> getSupportedMethods();
	

	/**
	 * Gets the {@link RestRequestHandler} configured to the given URL path.
	 * @return
	 */
	RestRequestHandler<T> getRestRequestHandler();
	

	/**
	 * Gets the {@link RestRequest} entity type to be created from the request parameters.
	 * @return
	 */
	Class<T> getRestRequestType();
	
	
	/**
	 * Gets the authorization context type.
	 * @return The authorization context type.
	 */
	AuthorizationContextType getAuthorizationContext();
	
	/**
	 * returns the default assembly which is there to hold default values that should be used when no value was given explicitly
	 * @return
	 */
	T getDefaultRequest();
}
