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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRuleSet;

/**
 * basic (ioc based) implementation of the {@link StateChangeProcessorRuleSet}<br/>
 * <br/>
 * in order to retain the order of the rules supplied, and to get a quick access to the 
 * stored rule via its processor id, both list and map are sustained.
 *<br/><br/>
 * @author pit
 * @author dirk
 *
 */
public class ConfigurableStateChangeProcessorRuleSet implements StateChangeProcessorRuleSet {

	protected List<StateChangeProcessorRule> rules;
	protected Map<String, StateChangeProcessorRule> idToRuleMap = new HashMap<String, StateChangeProcessorRule>();
	
	public void setProcessorRules(List<StateChangeProcessorRule> rules) {
		this.rules = rules;
		// clear the currently stored rules .. 
		idToRuleMap.clear();
		for (StateChangeProcessorRule rule : rules) {
			idToRuleMap.put( rule.getRuleId(), rule);
		}
	}
	
	@Override
	public List<StateChangeProcessorRule> getProcessorRules() {		
		return rules;
	}

	@Override
	public StateChangeProcessorRule getProcessorRule(String processorId) throws StateChangeProcessorException {
		StateChangeProcessorRule rule = idToRuleMap.get( processorId);
		if (rule != null)
			return rule;
		throw new StateChangeProcessorException("no rule found with processor id [" + processorId + "]");
	}

}
