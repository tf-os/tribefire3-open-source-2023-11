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

import java.util.List;

import com.braintribe.model.generic.GenericEntity;

/**
 * the basic rule that defines a case where a {@link StateChangeProcessor} should
 * be called. <br/><br/>
 * consist of the {@link StateChangeProcessor} reference and {@link StateChangeProcessorSelector}
 * reference that does the matching <br/>
 * <br/>
 * it also tells the caller via the {@link ProcessorExecutionMode} how it should be called, directory or scheduled.<br/><br/>
 * additionally, it defines an unique id as a {@link String} for the processor, so the rule can be identify across 
 * threads and even processor nodes (other JVMs in case of JMS for instance)<br/>  
 * 
 * @author pit
 * @author dirk
 */
public interface StateChangeProcessorRule {
	
	/**
	 * @return - the unique id of the rule the processors are contained within
	 */
	public String getRuleId();
	
	/**
	 * @return - the processor we need to call 
	 */
	public StateChangeProcessor<? extends GenericEntity, ?> getStateChangeProcessor(String processorId);
	
	public List<StateChangeProcessorMatch> matches( StateChangeProcessorSelectorContext context);
}
