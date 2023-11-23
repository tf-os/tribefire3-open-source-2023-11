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

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.mpc.quantifier.MpcQuantifier;
import com.braintribe.model.mpc.quantifier.MpcQuantifierStrategy;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluator;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorContext;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.api.multievaluation.MpcEvaluationResumptionStrategy;
import com.braintribe.model.processing.mpc.evaluator.impl.MpcMatchImpl;

/**
 * {@link MpcEvaluator} for {@link MpcQuantifier}
 * 
 */
public class MpcQuantifierEvaluator implements MpcEvaluator<MpcQuantifier> {

	private static Logger logger = Logger.getLogger(MpcQuantifierEvaluator.class);
	private static boolean trace = logger.isTraceEnabled();
	private static boolean debug = logger.isDebugEnabled();
	
	@Override
	public MpcMatch matches(MpcEvaluatorContext context, MpcQuantifier condition, IModelPathElement element)
			throws MpcEvaluatorRuntimeException {

		Integer minRepetition = condition.getMinimumRepetition();
		Integer maxRepetition = condition.getMaximumRepetition();
		if(trace) logger.trace("get min and max repetitions" + minRepetition + " " + maxRepetition);
		
		
		if(debug) logger.debug("validate min and max repetitions");
		validateMinMax(minRepetition, maxRepetition);

		boolean anotherEvaluationAttempt = false;

		if(debug) logger.debug("get state that holds the required variables for algorithm to work");
		MpcQuantifierPotentialMatch $ = context.getPreservedState(this, condition);

		if(trace) logger.trace(" if there is no preserved state, then this is a new entry : " +$);
		if ($ == null) {
			$ = new MpcQuantifierPotentialMatch(null);
		} else {
			anotherEvaluationAttempt = true;
		}

		if(trace) logger.trace("update strategy as per incoming condition");
		$.quantifierStrategy = condition.getQuantifierStrategy();

		if(debug) logger.debug("if this is not a back track visit to the evaluator :" + !anotherEvaluationAttempt);
		if (!anotherEvaluationAttempt) {
			if(debug) logger.debug(" set path to the input parameter as this is a fresh visit : " + element);
			$.currentPath = element;

		} else {
			MpcEvaluationResumptionStrategy resumptionStrategy = $.getResumptionStrategy();
			if(debug) logger.debug(" Adjust according to resumptionStrategy" + resumptionStrategy);
			
			switch (resumptionStrategy) {
				case maintainState:
					if(trace) logger.trace("return state as it is" + $);
					return $;
				case resumeProcessing:
					switch ($.quantifierStrategy) {
						case greedy:
								MpcMatch matchesResult = $.matchesList.remove($.matchesList.size() - 1);
								if(trace) logger.trace("consume one of the latest match " + matchesResult);
								$.setPath(matchesResult.getPath());
								return $;
						case possessive:
							logger.error("No roll back is possible with a possessive strategy");
							throw new MpcEvaluatorRuntimeException(
									"No roll back is possible with a possessive strategy");
						case reluctant:// leave path as it is
							if(trace) logger.trace(" leave path as it is");
							break;
						default:
							logger.error("Unknown quantifier strategy used");
							throw new MpcEvaluatorRuntimeException("Unknown quantifier strategy used");
					}
					break;
				default:
					break;
			}
		}

		if(trace) logger.trace("get condition that will be evaluated (possibly multiple evaluations");
		Object quantifiedCondition = condition.getCondition();

		if(trace) logger.trace("init flag to indicate if matching should be attempted");
		boolean attemptMatch = true;

		if(trace) logger.trace("init value that will be returned by this method");
		MpcMatch matchesResult = null;

		// flag indicates if "noMatch" is a valid return value to this method
		boolean noMatchIsValid = false;
		if(debug) logger.debug("if this is a fresh attempt and there are zero or null repetitions :" + minRepetition + " another attempt:" + anotherEvaluationAttempt);
		if (!anotherEvaluationAttempt && (minRepetition == null || minRepetition == 0)) {
			if(trace) logger.trace("a valid return value is the full original of the parameter, without any consumption at all" + element);
			matchesResult = new MpcMatchImpl(element);
			// decrement counter as this should not be counted as a valid match further on
			$.matchesCounter--;
			// activate flag
			noMatchIsValid = true;
		}

		boolean maxInvalid = (maxRepetition != null && maxRepetition == 0);
		if(trace) logger.trace("set flag to indicate if there are no repetitions needed, as max repetition is zero" + maxInvalid);

		while (attemptMatch) {

			// if it is not a trivial case either 1. match nothing, or 2. max repetitions is 0
			if(debug) logger.debug("if it is not a trivial case either 1. match nothing, or 2. max repetitions is 0 :" + !noMatchIsValid + " " + !maxInvalid);
			if (!noMatchIsValid && !maxInvalid) {
				if(debug) logger.debug("Invoke context matches");
				matchesResult = context.matches(quantifiedCondition, $.currentPath);
			}
			if(debug) logger.debug("Match result :" + matchesResult);
			if (matchesResult == null) {
				if(trace) logger.trace(" if greedy, then go one step back, if there are results and if min is ok (it will be ok if there are elements in the matchesList)" +  $.matchesList.size());
				if ($.quantifierStrategy == MpcQuantifierStrategy.greedy && $.matchesList.size() > 0) {
					if(trace) logger.trace("consume one element from the list of matches");
					matchesResult = $.matchesList.remove($.matchesList.size() - 1);
				}
				
				if(trace) logger.trace("there is no path to use later on (in Reluctant mode) as the current one has already failed");
				$.currentPath = null;
				if(trace) logger.trace("looping is done as result is found (null or some element from the matchesList)");
				attemptMatch = false;
			} else {
				if(trace) logger.trace("increment valid matches counter : " + $.matchesCounter);
				$.matchesCounter++;
				if(trace) logger.trace("set the path for next iteration (if any)");
				$.currentPath = matchesResult.getPath();

				boolean minRepsAchieved = (minRepetition == null || $.matchesCounter >= minRepetition);
				if(debug) logger.debug("flag to indicate if minimum repetitions has been achieved :" + minRepsAchieved);

				if (!minRepsAchieved) {
					if(debug) logger.debug(" if there is no path to continue processing :" + $.currentPath);
					if ($.currentPath == null) {
						matchesResult = null;
						attemptMatch = false;
						if(trace) logger.trace("result is null (even though there might have been a recent hit) and exit loop");
					}
				} else { 
					if(debug) logger.debug(" minimum threshold reached or exceeded");
					switch ($.quantifierStrategy) {
						case greedy:
							// add value to possible matches
							if(debug) logger.debug(" add value to possible matches");
							$.matchesList.add(matchesResult);
							break;
						case possessive:
							if(trace) logger.trace("do nothing");
							break;
						case reluctant:
							if(debug) logger.debug(" this iteration is done (reluctant)");
							attemptMatch = false;
							break;
						default:
							throw new MpcEvaluatorRuntimeException("Unknown quantifier strategy used");
					}

					boolean maxRepsAchieved = (maxRepetition != null && $.matchesCounter == maxRepetition);
					if(debug) logger.debug("flag to indicate if maximum repetitions has been achieved :" + maxRepsAchieved);
					
					if (maxRepsAchieved || $.currentPath == null) {
						if(trace) logger.trace(" we have reached the maximum or there is no more path to traverse");
						attemptMatch = false;
						$.currentPath = null;
						if(trace) logger.trace("loop is done and  set path to null, in case there is still path to consume, but max has been reached ");
					}
				}
			}
			if(trace) logger.trace("set \"NoMatch\" flag to false");
			noMatchIsValid = false;
		}

		if(debug) logger.debug("matchesResult :" + matchesResult);
		if (matchesResult == null) {
			return matchesResult;
		} else {
			$.setPath(matchesResult.getPath());
			return $;
		}
	}

	/**
	 * Validates that min and max repetitions are within expected ranges
	 * @param minRepetition
	 * @param maxRepetition
	 * @throws MpcEvaluatorRuntimeException
	 * 		if there is an invalid range
	 */
	private static void validateMinMax(Integer minRepetition, Integer maxRepetition)
			throws MpcEvaluatorRuntimeException {
		if ((maxRepetition != null && maxRepetition < 0) || (minRepetition != null && minRepetition < 0)) {
			throw new MpcEvaluatorRuntimeException("Min and Max repetitions should be >= 0 when not null");
		}
		if ((maxRepetition != null && minRepetition != null && maxRepetition < minRepetition)) {
			throw new MpcEvaluatorRuntimeException("Max Repition should be greater than or equal Min repetition");
		}
	}

	@Override
	public boolean allowsPotentialMatches(MpcQuantifier condition) {
		
		if(condition.getQuantifierStrategy() == MpcQuantifierStrategy.possessive)
		{
			return false;
		}
		
		Integer maxRepetition = condition.getMaximumRepetition();
		Integer minRepetition = condition.getMinimumRepetition();
		
		if(maxRepetition != null && minRepetition != null && maxRepetition.intValue() == minRepetition.intValue()){
			return false;
		}

		return true;
	}

	@Override
	public boolean hasNestedConditions(MpcQuantifier condition) {
		return true;
	}

}
