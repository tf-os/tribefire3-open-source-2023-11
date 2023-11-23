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
package tribefire.extension.tracing.model.service.configuration;

import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.InstanceId;

import tribefire.extension.tracing.model.service.MulticastTracingResult;
import tribefire.extension.tracing.model.service.configuration.local.InitializeTracingLocalResult;

public interface InitializeTracingResult extends MulticastTracingResult {

	EntityType<InitializeTracingResult> T = EntityTypes.T(InitializeTracingResult.class);

	String results = "results";

	Map<InstanceId, InitializeTracingLocalResult> getResults();
	void setResults(Map<InstanceId, InitializeTracingLocalResult> results);
}
