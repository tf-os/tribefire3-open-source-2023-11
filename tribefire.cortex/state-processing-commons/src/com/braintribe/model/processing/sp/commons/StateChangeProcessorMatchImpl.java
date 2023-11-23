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
package com.braintribe.model.processing.sp.commons;

import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMatch;



/**
 * 
 * @author pit, dirk
 *
 */
public class StateChangeProcessorMatchImpl implements StateChangeProcessorMatch {
	private String processorId;
	private StateChangeProcessor<?, ?> stateChangeProcessor;
	
	public StateChangeProcessorMatchImpl( String processorId, StateChangeProcessor<?,?> stateChangeProcessor) {
		this.processorId = processorId;
		this.stateChangeProcessor = stateChangeProcessor;
	}

	/* (non-Javadoc)
	 * @see com.braintribe.model.processing.sp.api.StateChangeProcessorMatch#getProcessorId()
	 */
	@Override
	public String getProcessorId() {
		return processorId;
	}

	/* (non-Javadoc)
	 * @see com.braintribe.model.processing.sp.api.StateChangeProcessorMatch#getStateChangeProcessor()
	 */
	@Override
	public StateChangeProcessor<?, ?> getStateChangeProcessor() {
		return stateChangeProcessor;
	}

}
