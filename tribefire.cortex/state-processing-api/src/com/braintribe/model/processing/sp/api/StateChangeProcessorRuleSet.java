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

/**
 * the basic rule set interface 
 * 
 * @author pit
 * @author dirk
 *
 */
public interface StateChangeProcessorRuleSet {

	/**
	 * @return - the list of all rules in the set 
	 */
	public List<StateChangeProcessorRule> getProcessorRules();
	/**
	 * @param processorId - the id of the processor the rule is attached to 
	 * @return - the rule associated with the id 
	 * @throws StateChangeProcessorException - if no rule with that processor id has been found 
	 */
	public StateChangeProcessorRule getProcessorRule( String processorId) throws StateChangeProcessorException;  
}
