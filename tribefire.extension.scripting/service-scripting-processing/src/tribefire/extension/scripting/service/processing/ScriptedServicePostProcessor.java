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
package tribefire.extension.scripting.service.processing;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ReasonedServicePostProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.scripting.common.CommonScriptedProcessor;

/**
 * Expert implementation to provide a {@link ScriptedServicePostProcessor} based on {@link CommonScriptedProcessor}. 
 * The bindings (parameter input to script) contain
 * <ul>
 * <li> {@link ServiceRequestContext} as "$context" </li>
 * <li> {@link Object} as "$response" (depends on script return type) </li> 
 * </ul> 
 * 
 *
 */
public class ScriptedServicePostProcessor extends CommonScriptedProcessor implements ReasonedServicePostProcessor<Object> {

	@Override
	public Maybe<? extends Object> processReasoned(ServiceRequestContext requestContext, Object response) {

		Map<String, Object> bindings = new HashMap<>();
		bindings.put("$context", requestContext);
		bindings.put("$response", response);

		return processReasonedScripted(bindings);
	}
}
