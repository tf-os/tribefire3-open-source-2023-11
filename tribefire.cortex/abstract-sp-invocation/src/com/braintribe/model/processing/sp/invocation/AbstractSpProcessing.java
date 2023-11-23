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
package com.braintribe.model.processing.sp.invocation;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.sp.api.ProcessStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;

/**
 * processes a call of a state processor 
 * 
 * @author pit
 * @author dirk
 *
 */
public abstract class AbstractSpProcessing {
	
	private static Logger log = Logger.getLogger(AbstractSpProcessing.class);
	
	
	/**
	 * synchronously (directly) process a state change processor 
	 * @param processorId - the id of the processor (only needed for error message purposes) 
	 * @param processor - the processor to call
	 * @param stateChangeContext - the associated context 
	 * @param customData - the data packet that the processor generated in its {@link StateChangeProcessor#onBeforeStateChange(StateChangeContext)}, if any 
	 */
	protected void process( String ruleId, String processorId, StateChangeProcessor<GenericEntity, GenericEntity> processor, ProcessStateChangeContext<GenericEntity> stateChangeContext, GenericEntity customData){
		try {			
			processor.processStateChange(stateChangeContext, customData);
			stateChangeContext.commitIfNecessary();			
		} catch (StateChangeProcessorException e) {
			String msg = "error while executing process of processor [" + processorId + "] of rule [" + ruleId +"]";
			log.error(msg, e);						
		} catch (GmSessionException e) {
			String msg = "error while executing process of processor [" + processorId + "] of rule [" + ruleId +"]";
			log.error(msg, e);	
		}
	}
}
