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

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMapping;
import com.braintribe.model.stateprocessing.api.SelectiveStateChangeProcessorAddressing;

public class ConfigurableStateChangeProcessorMapping implements StateChangeProcessorMapping {
	private StateChangeProcessor<?, ?> stateChangeProcessor;
	private SelectiveStateChangeProcessorAddressing stateChangeProcessorAddressing;
	
	@Configurable @Required
	public void setStateChangeProcessor( StateChangeProcessor<?, ?> stateChangeProcessor) {
		this.stateChangeProcessor = stateChangeProcessor;
	}
	
	@Configurable @Required
	public void setStateChangeProcessorAddressing( SelectiveStateChangeProcessorAddressing stateChangeProcessorAddressing) {
		this.stateChangeProcessorAddressing = stateChangeProcessorAddressing;
	}
	
	@Override
	public StateChangeProcessor<?, ?> getProcessor() {
		return stateChangeProcessor;
	}

	@Override
	public SelectiveStateChangeProcessorAddressing getProcessorAddressing() {
		return stateChangeProcessorAddressing;
	}

}
