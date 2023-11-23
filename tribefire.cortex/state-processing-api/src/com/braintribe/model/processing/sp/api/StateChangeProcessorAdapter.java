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

import com.braintribe.model.generic.GenericEntity;

public class StateChangeProcessorAdapter<T extends GenericEntity, C extends GenericEntity> implements StateChangeProcessor<T, C>{

	@Override
	public C onBeforeStateChange(BeforeStateChangeContext<T> context) throws StateChangeProcessorException {
		return null;
	}

	@Override
	public void onAfterStateChange(AfterStateChangeContext<T> context, C customContext) throws StateChangeProcessorException {
		// blank
	}

	@Override
	public void processStateChange(ProcessStateChangeContext<T> context, C customContext) throws StateChangeProcessorException {
		// blank
	}
}
