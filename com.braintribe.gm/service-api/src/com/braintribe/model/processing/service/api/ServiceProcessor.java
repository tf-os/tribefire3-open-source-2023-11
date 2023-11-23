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
package com.braintribe.model.processing.service.api;

import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * Managed processor of {@link ServiceRequest} instances.
 * 
 * <h1>Naming convention</h1>
 * 
 * <p>
 * Specializations are encouraged to keep the suffixes {@code ServiceProcessor} (or simply {@code Processor}),
 * {@code Request} and {@code Response} for their respective {@link ServiceProcessor}, {@link ServiceRequest} and
 * response types, thus following signature pattern:
 * 
 * <p>
 * <i>{purpose}</i>ServiceProcessor implements ServiceProcessor<<i>{purpose}</i>Request, <i>{purpose}</i>Response>
 * 
 * <p>
 * e.g.: <br>
 * {@code SecurityServiceProcessor implements ServiceProcessor<SecurityRequest, SecurityResponse>}
 * 
 * <p>
 * For processors leveraging on polymorphism to address multiple tasks, request specializations may be named like
 * functions.
 * 
 * <p>
 * e.g.:
 * <ul>
 * <li>{@code Logout extends SecurityRequest}</li>
 * <li>{@code OpenUserSession extends SecurityRequest}</li>
 * <li>{@code ValidateUserSession extends SecurityRequest}</li>
 * </ul>
 * 
 * @author dirk.scheffler
 * 
 * @param <P>
 *            The input ({@link ServiceRequest}) type
 * @param <R>
 *            The return type
 */
public interface ServiceProcessor<P extends ServiceRequest, R extends Object> {

	/**
	 * <p>
	 * Processes the given {@code request}.
	 * 
	 * @param requestContext
	 *            A {@link ServiceRequestContext} providing contextual information about the request.
	 * @param request
	 *            The {@link ServiceRequest} to be processed.
	 * @return The result of the {@link ServiceRequest} processing.
	 */
	R process(ServiceRequestContext requestContext, P request);

}
