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
package tribefire.platform.impl.deployment;

import java.util.Collections;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.BeforeStateChangeContext;
import com.braintribe.model.processing.sp.api.ProcessStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMatch;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorSelectorContext;

public class NoopStateChangeProcessorRule implements StateChangeProcessorRule, StateChangeProcessor<GenericEntity, GenericEntity> {

	public static final NoopStateChangeProcessorRule instance = new NoopStateChangeProcessorRule();
	
	
	@Override
	public GenericEntity onBeforeStateChange(BeforeStateChangeContext<GenericEntity> context) throws StateChangeProcessorException {
		return null;
	}

	@Override
	public void onAfterStateChange(AfterStateChangeContext<GenericEntity> context, GenericEntity customContext) throws StateChangeProcessorException {
		//intentionally left empty
	}

	@Override
	public void processStateChange(ProcessStateChangeContext<GenericEntity> context, GenericEntity customContext) throws StateChangeProcessorException {
		//intentionally left empty
	}

	@Override
	public String getRuleId() {
		return null;
	}

	@Override
	public StateChangeProcessor<? extends GenericEntity, ? extends GenericEntity> getStateChangeProcessor(String processorId) {
		return this;
	}

	@Override
	public List<StateChangeProcessorMatch> matches(StateChangeProcessorSelectorContext context) {
		return Collections.emptyList();
	}
	
}
