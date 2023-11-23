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
package com.braintribe.model.processing.mpc.evaluator.impl.quantifier;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.mpc.quantifier.MpcQuantifierStrategy;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.api.multievaluation.MpcEvaluationResumptionStrategy;
import com.braintribe.model.processing.mpc.evaluator.api.multievaluation.MpcPotentialMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.MpcMatchImpl;

public class MpcQuantifierPotentialMatch extends MpcMatchImpl implements MpcPotentialMatch {

	private static Logger logger = Logger.getLogger(MpcQuantifierPotentialMatch.class);
	private static boolean trace = logger.isTraceEnabled();
	
	public List<MpcMatch> matchesList;
	public MpcQuantifierStrategy quantifierStrategy;
	public int matchesCounter;
	public IModelPathElement currentPath;
	
	
	private MpcEvaluationResumptionStrategy resumptionStrategy ;
	
	public MpcQuantifierPotentialMatch(IModelPathElement element) {
		super(element); 
		matchesList = new ArrayList<MpcMatch>();
		quantifierStrategy = MpcQuantifierStrategy.greedy;
		matchesCounter = 0;
		currentPath = null;
	}

	public MpcQuantifierPotentialMatch(IModelPathElement element, List<MpcMatch> matchesList, int matchesCounter, IModelPathElement currentPath, MpcQuantifierStrategy quantifierStrategy) {
		super(element); 
		this.matchesList = matchesList;
		this.matchesCounter = matchesCounter;
		this.currentPath = currentPath;
		this.quantifierStrategy = quantifierStrategy;
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public boolean hasAnotherAttempt() {
		boolean backTrackingPossible = false;
		switch (quantifierStrategy) {
			case greedy:
				if (trace) logger.trace("if there are any previous matches, then we can backtrack and use them :" + matchesList + " size "+ (matchesList != null? matchesList.size() : "NO"));
				backTrackingPossible = (matchesList != null && matchesList.size() > 0) ? true : false;
				break;
			case reluctant:
				if (trace) logger.trace("if there is unconsumed path, it can be used for further processing :" + currentPath );
				backTrackingPossible = currentPath != null ? true : false;
				break;
		}
		return backTrackingPossible;
	}

	@Override
	public void setResumptionStrategy(MpcEvaluationResumptionStrategy resumptionStrategy){
		this.resumptionStrategy = resumptionStrategy;
	}
	
	@Override
	public MpcEvaluationResumptionStrategy getResumptionStrategy() {
		return resumptionStrategy;
	}


}
