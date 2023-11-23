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
package com.braintribe.model.processing.sp.api;

import com.braintribe.model.stateprocessing.api.OnAfterStateChangeProcessingRequest;
import com.braintribe.model.stateprocessing.api.OnAfterStateChangeProcessingResponse;
import com.braintribe.model.stateprocessing.api.OnBeforeStateChangeProcessingRequest;
import com.braintribe.model.stateprocessing.api.OnBeforeStateChangeProcessingResponse;
import com.braintribe.model.stateprocessing.api.StateChangeProcessingRequest;
import com.braintribe.model.stateprocessing.api.StateChangeProcessingResponse;

public interface StateChangeProcessorService {

	StateChangeProcessingResponse processStateChange(StateChangeProcessingRequest request) throws StateChangeProcessorException;
	OnBeforeStateChangeProcessingResponse onBeforeStateChange(OnBeforeStateChangeProcessingRequest request) throws StateChangeProcessorException;
	OnAfterStateChangeProcessingResponse onAfterStateChange(OnAfterStateChangeProcessingRequest request) throws StateChangeProcessorException;
}
