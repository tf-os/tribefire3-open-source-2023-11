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
package com.braintribe.model.processing.mpc.evaluator.impl.multievaluation;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.mpc.evaluator.api.multievaluation.MpcMultiEvaluationState;
import com.braintribe.model.processing.mpc.evaluator.api.multievaluation.MpcNestedConditionExpertScope;
import com.braintribe.model.processing.mpc.evaluator.api.multievaluation.MpcPotentialMatch;

/**
 * Linked List implementation of {@link MpcNestedConditionExpertScope}
 */
public class MpcNestedConditionsExpertScopeImpl implements MpcNestedConditionExpertScope {

	private static Logger logger = Logger.getLogger(MpcNestedConditionsExpertScopeImpl.class);
	private static boolean trace = logger.isTraceEnabled();

	
	MpcMultiEvaluationState anchorBacktrackingState;
	MpcMultiEvaluationState currentBacktrackingState;
	
	
	public MpcNestedConditionsExpertScopeImpl next;
	public MpcNestedConditionsExpertScopeImpl previous;
	public int length;
	
	// TODO maybe add optimisation to handle the case of saving the results when it is not needed to continue
	
	public MpcNestedConditionsExpertScopeImpl(){
		anchorBacktrackingState = new MpcMultiEvaluationState();
		currentBacktrackingState = anchorBacktrackingState;
	}
	

	@Override
	public void incrementMultiEvaluationExpertIndex() {
		if (currentBacktrackingState.next == null) {	
			MpcMultiEvaluationState tempState = new MpcMultiEvaluationState();
			tempState.previous = currentBacktrackingState;
			currentBacktrackingState.next = tempState;
		}
		
		currentBacktrackingState = currentBacktrackingState.next;
	}

	@Override
	public boolean isCurrentMultiEvaluationExpertActive() {
		return currentBacktrackingState.isActive;
	}

	@Override
	public boolean isCurrentMultiEvaluationExpertNew() {
		if(currentBacktrackingState.potentialMatch == null){
			return true;
		}
		return false;
	}

	@Override
	public MpcPotentialMatch getMultiEvaluationExpertState() {		
		return currentBacktrackingState.potentialMatch;
	}

	@Override
	public void setMultiEvaluationExpertState(MpcPotentialMatch potentialMatch) {
		currentBacktrackingState.potentialMatch = potentialMatch;
	}

	@Override
	public boolean isUnexploredPathAvailable() {
		MpcMultiEvaluationState currentState = getLastStateAndResetActiveForAll();
		
		boolean result = false;
		if(trace) logger.trace("loop through all");
		while (currentState != null){
			if(!result){
				if(trace) logger.trace("currentState possible:" + currentState.hasAnotherProcessingAttempt());
				if(currentState.hasAnotherProcessingAttempt()){
					currentState.isActive = true;
					result = true;
					break;
				}
				else{
					logger.trace("reset value as it will have new data");
					currentState.potentialMatch = null;
				}
			}
			
			currentState = currentState.previous;
		}
		return result;
	}
	
	private MpcMultiEvaluationState getLastStateAndResetActiveForAll(){
		MpcMultiEvaluationState currentState = anchorBacktrackingState.next;
		MpcMultiEvaluationState lastState = anchorBacktrackingState.next;
		if(trace) logger.trace("Identify last state");
		while(currentState != null){
			currentState.isActive = false;
			if(currentState.next == null){
				lastState = currentState;
			}
			currentState = currentState.next;
		}
		
		return lastState;
	}
	
	@Override
	public void resetIteration() {
		currentBacktrackingState = anchorBacktrackingState;
	}

}
