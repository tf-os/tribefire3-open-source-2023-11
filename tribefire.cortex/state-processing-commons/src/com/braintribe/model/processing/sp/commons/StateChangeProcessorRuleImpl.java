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

import java.util.Collections;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.processing.typecondition.experts.GenericTypeConditionExpert;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMatch;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorSelectorContext;


/**
 * implements both {@link StateChangeProcessorRule} and {@link StateChangeProcessorMatch} <br/>
 * <br/>
 * {@link #matches(StateChangeProcessorSelectorContext)} tests on the correct entity type and property name
 * 
 * @author pit
 * @author dirk
 *
 */
public class StateChangeProcessorRuleImpl implements StateChangeProcessorRule, StateChangeProcessorMatch {

	private StateChangeProcessor<? extends GenericEntity, ? extends GenericEntity> stateChangeProcessor;
	private String stateProperty;
	private TypeCondition typeCondition;
	private String processorId;
	
	@Override
	public StateChangeProcessor<? extends GenericEntity, ? extends GenericEntity> getStateChangeProcessor() {
		return stateChangeProcessor;
	}
	
	public void setStateChangeProcessor( StateChangeProcessor<? extends GenericEntity, ? extends GenericEntity> stateChangeProcessor) {
		this.stateChangeProcessor = stateChangeProcessor;
	}
	
	public String getStateProperty() {	
		return stateProperty;
	}
	public void setStateProperty(String stateProperty) {
		this.stateProperty = stateProperty;
	}

	public TypeCondition getInstanceTypeCondition() {		
		return typeCondition;
	}
	public void setTypeCondition(TypeCondition typeCondition) {
		this.typeCondition = typeCondition;
	}
	
			
	@Override
	public String getRuleId() {
		return processorId;
	}
	
	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}

	@Override
	public String getProcessorId() {		
		return processorId;
	}

	@Override
	public StateChangeProcessor<? extends GenericEntity, ? extends GenericEntity> getStateChangeProcessor( String processorId) {
		return stateChangeProcessor;
	}

	@Override
	public List<StateChangeProcessorMatch> matches(StateChangeProcessorSelectorContext context) {
		if	(matchesPropertyCondition(context) && matchesTypeCondition(context)) {
			return Collections.<StateChangeProcessorMatch>singletonList( this);
		} else {
			return Collections.emptyList();
		}
	}

	private boolean matchesTypeCondition(StateChangeProcessorSelectorContext context) {
		if (typeCondition == null)
			return true;
		
		return GenericTypeConditionExpert.getDefaultInstance().matchesTypeCondition(typeCondition, context.getEntityType());
	}

	private boolean matchesPropertyCondition(StateChangeProcessorSelectorContext context) {
		if (!context.isForProperty())
			return false;
		
		String statePropertyName = getStateProperty();
		
		if (statePropertyName == null)
			return true;
		
		String propertyName = context.getProperty().getName();
		return propertyName.equals(statePropertyName);
	}
}
