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
package com.braintribe.model.processing.platformsetup;

import java.util.Collections;
import java.util.List;

import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMatch;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorSelectorContext;

/**
 * TODO: Find out how to track deep-structure changes as well.
 */
public class PlatformSetupStateChangeProcessor implements StateChangeProcessor<PlatformAsset, GenericEntity>, StateChangeProcessorRule, StateChangeProcessorMatch {

	private static Property hasUnsavedChangesProperty = PlatformAsset.T.getProperty(PlatformAsset.hasUnsavedChanges);
	private static Property resolvedRevisionProperty = PlatformAsset.T.getProperty(PlatformAsset.resolvedRevision);
	
	@Override
	public String getRuleId() {
		return getClass().getName();
	}

	@Override
	public StateChangeProcessor<? extends GenericEntity, ?> getStateChangeProcessor(String processorId) {
		return this;
	}
	
	@Override
	public String getProcessorId() {
		return getRuleId();
	}
	
	@Override
	public StateChangeProcessor<?, ?> getStateChangeProcessor() {
		return this;
	}

	@Override
	public List<StateChangeProcessorMatch> matches(StateChangeProcessorSelectorContext context) {
		if (context.isForProperty() && context.getEntityType() == PlatformAsset.T && context.getProperty() != hasUnsavedChangesProperty
				&& context.getProperty() != resolvedRevisionProperty) {
			return Collections.singletonList(this);
		} else {
			return Collections.emptyList();
		}
	}

	
	@Override
	public void onAfterStateChange(AfterStateChangeContext<PlatformAsset> context, GenericEntity customContext) throws StateChangeProcessorException {
		context.getProcessEntity().setHasUnsavedChanges(true);
	}

}
