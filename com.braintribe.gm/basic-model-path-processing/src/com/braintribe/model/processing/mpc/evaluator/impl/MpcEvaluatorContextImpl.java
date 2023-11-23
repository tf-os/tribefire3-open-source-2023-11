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
package com.braintribe.model.processing.mpc.evaluator.impl;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.mpc.ModelPathCondition;
import com.braintribe.model.mpc.value.MpcElementValue;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluator;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorContext;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.mpc.evaluator.api.MpcMatch;
import com.braintribe.model.processing.mpc.evaluator.api.MpcRegistry;
import com.braintribe.model.processing.mpc.evaluator.api.multievaluation.MpcEvaluationResumptionStrategy;
import com.braintribe.model.processing.mpc.evaluator.api.multievaluation.MpcNestedConditionExpertScope;
import com.braintribe.model.processing.mpc.evaluator.api.multievaluation.MpcPotentialMatch;
import com.braintribe.model.processing.mpc.evaluator.impl.builder.MpcRegistryBuilderImpl;
import com.braintribe.model.processing.mpc.evaluator.impl.generic.MpcGenericVde;
import com.braintribe.model.processing.mpc.evaluator.impl.multievaluation.MpcNestedConditionsExpertScopeImpl;
import com.braintribe.model.processing.mpc.evaluator.impl.value.vde.MpcElementValueVde;
import com.braintribe.model.processing.mpc.evaluator.impl.value.vde.aspect.MpcElementValueAspect;
import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.vde.evaluator.api.builder.VdeContextBuilder;
import com.braintribe.model.processing.vde.evaluator.api.builder.VdeRegistryBuilder;

/**
 * Implementation of {@link MpcEvaluatorContext}
 * 
 */

public class MpcEvaluatorContextImpl implements MpcEvaluatorContext {
	private static Logger logger = Logger.getLogger(MpcEvaluatorContextImpl.class);
	private static boolean debug = logger.isDebugEnabled();
	private static boolean trace = logger.isTraceEnabled();

	private MpcRegistry registry = null;

	private MpcNestedConditionsExpertScopeImpl expertScope;

	private VdeContextBuilder vdeContextBuilder;
	private VdeRegistryBuilder vdeRegistryBuilder;

	public MpcEvaluatorContextImpl() {
		if (this.registry == null) {
			this.registry = new MpcRegistryBuilderImpl().defaultSetup();
		}

		this.expertScope = null;
		initVdeContext();
	}

	private void initVdeContext() {
		this.vdeRegistryBuilder = VDE.registryBuilder();
		this.vdeRegistryBuilder.loadDefaultSetup().withConcreteExpert(MpcElementValue.class, new MpcElementValueVde());
		this.vdeContextBuilder = VDE.evaluate();
	}

	@Override
	public MpcMatch matches(Object condition, IModelPathElement element) throws MpcEvaluatorRuntimeException {

		if (condition instanceof ModelPathCondition) {
			return matches((ModelPathCondition) condition, element);
		} else {
			return vdeMatches(condition, element);
		}
	}

	private MpcMatch vdeMatches(Object condition, IModelPathElement element) throws MpcEvaluatorRuntimeException {

		vdeRegistryBuilder.withAbstractExpert(ModelPathCondition.class, new MpcGenericVde(this));
		Object evaluationResult = vdeContextBuilder.withRegistry(vdeRegistryBuilder.done()).with(MpcElementValueAspect.class, element).forValue(condition);

		if (evaluationResult instanceof MpcMatch) {
			return (MpcMatch) evaluationResult;
		} else if (evaluationResult instanceof Boolean) {

			if ((Boolean) evaluationResult) { // default behaviour is to capture
												// one element only
				MpcMatchImpl result = new MpcMatchImpl(element.getPrevious());
				return result;
			}
			return null;
		}
		throw new MpcEvaluatorRuntimeException("VDE evaluation of " + condition + " yielded a non-Boolean value");
	}

	public MpcMatch matches(ModelPathCondition condition, IModelPathElement element) throws MpcEvaluatorRuntimeException {

		if (debug)
			logger.debug("Identify required evaluator");
		MpcEvaluator<ModelPathCondition> expert = findExpertFor(condition);
		if (trace)
			logger.trace("Check if the current expert invokes context matches internally");
		boolean expertHasNestedConditions = expert.hasNestedConditions(condition);

		if (expertHasNestedConditions) {
			if (debug)
				logger.debug("Create a new evaluation state");
			extendScope();
		}

		if (trace)
			logger.trace("Initilaise flag to indicate if processing should continue/stop, usually processing stops unless there is another backtracking attempts that might be invoked");
		boolean continueProcessing = true;
		if (trace)
			logger.trace("Initilaise result of method");
		MpcMatch result = null;

		while (continueProcessing) {
			if (trace)
				logger.trace("Start of loop iteration");
			if (debug)
				logger.debug("Match with expert");
			result = expert.matches(this, condition, element);
			if (debug)
				logger.debug("Store the state of the result, if needed");
			preserveState(result, expert, condition);
			if (debug)
				logger.debug("Check if more processing is required");
			continueProcessing = isFurtherProcessingNeeded(result, expert, condition);
			if (trace)
				logger.trace("end of loop iteration");
		}

		if (debug)
			logger.debug("Regardless of the result of the evaluation, check if we have context invoker expert");
		if (expertHasNestedConditions) {
			if (debug)
				logger.debug("Done with this evaluator, so remove it from list of evaluators");
			cleanUpEvaluatorWithNestedConditions();
		}
		return result;
	}

	/**
	 * Remove scope of the last evaluator with nested conditions
	 */
	private void cleanUpEvaluatorWithNestedConditions() {
		if (trace)
			logger.trace("start cleanup");
		expertScope = expertScope.previous;
		if (expertScope != null) {
			if (trace)
				logger.trace("Clean non-root scope");
			expertScope.next = null;
		}
	}

	/**
	 * Identifies the expert needed for the provided {@link ModelPathCondition}
	 * 
	 * @param condition
	 *            The ModelPathCondition that requires an expert
	 * @return The evaluator associated with the provided ModelPathCondition
	 * @throws MpcEvaluatorRuntimeException
	 */
	private MpcEvaluator<ModelPathCondition> findExpertFor(ModelPathCondition condition) throws MpcEvaluatorRuntimeException {

		if (trace)
			logger.trace("Get java type of entity Condition" + condition);
		Class<?> currentConditionType = GMF.getTypeReflection().getEntityType(condition).getJavaType();

		@SuppressWarnings("unchecked")
		MpcEvaluator<ModelPathCondition> result = (MpcEvaluator<ModelPathCondition>) this.registry.getExperts().get(currentConditionType);

		if (result == null) {
			logger.error("No evaluator found for ModelPathCondition: " + currentConditionType.getName());
			throw new MpcEvaluatorRuntimeException("No evaluator found for ModelPathCondition: " + currentConditionType.getName());
		}

		return result;
	}

	/**
	 * Identifies if further processing is needed based on the type of the
	 * {@link MpcEvaluator} and the value of {@link MpcMatch}.
	 * 
	 * @param iterationResult
	 *            {@link MpcMatch} that holds the current processing iteration
	 *            result
	 * @param expert
	 *            {@link MpcEvaluator} which has finished a processing iteration
	 * @param condition
	 *            {@link ModelPathCondition} that has been evaluated
	 * @return true if {@link MpcEvaluator} has nested conditions and there is
	 *         {@link MpcNestedConditionExpertScope#isUnexploredPathAvailable()}
	 *         evaluates to true
	 */
	private <C extends ModelPathCondition> boolean isFurtherProcessingNeeded(MpcMatch iterationResult, MpcEvaluator<C> expert, C condition) {

		boolean result = false;
		boolean expertHasNestedConditions = expert.hasNestedConditions(condition);

		if (trace)
			logger.trace("Check if further processing is needed for result:" + iterationResult + " and expert with Nested Conditions:" + expertHasNestedConditions);
		if (expertHasNestedConditions && iterationResult == null) {
			if (trace)
				logger.trace("Check if there are unexplored paths with respect to the expert");
			result = expertScope.isUnexploredPathAvailable();
		}

		if (trace)
			logger.trace("Check if another iteration is needed for the context invoker expert: " + result);
		if (result && expertHasNestedConditions) {
			if (trace)
				logger.trace("reset iteration");
			expertScope.resetIteration();
		}
		return result;
	}

	/**
	 * Increase the scope of the value of the
	 * {@link MpcNestedConditionExpertScope}
	 */
	private void extendScope() {

		MpcNestedConditionsExpertScopeImpl currentScope = new MpcNestedConditionsExpertScopeImpl();

		if (expertScope == null) {
			if (trace)
				logger.trace("Root Scope");
			expertScope = currentScope;
			expertScope.length = 1;
		} else {
			if (trace)
				logger.trace("Non-Root Scope");
			expertScope.next = currentScope;
			currentScope.previous = expertScope;
			currentScope.length = expertScope.length + 1;
			expertScope = currentScope;
		}
		if (trace)
			logger.trace("Scope length:" + expertScope.length);
	}

	/**
	 * Stores the current {@link MpcMatch} as the state of {@link MpcEvaluator}
	 * if {@link MpcEvaluator#allowsPotentialMatches(ModelPathCondition)} is
	 * true with respect to the current {@link MpcNestedConditionExpertScope}
	 * 
	 * @param result
	 *            {@link MpcMatch} value of the last iteration result
	 * @param expert
	 *            {@link MpcEvaluator} that computed the result to be stored
	 * @param condition
	 *            {@link ModelPathCondition} that has been evaluated with
	 *            {@link MpcEvaluator}
	 */
	private <C extends ModelPathCondition> void preserveState(MpcMatch result, MpcEvaluator<C> expert, C condition) {

		boolean multiEvaluationExpert = expert.allowsPotentialMatches(condition);

		if (trace)
			logger.trace("Check if expert allows multiple Evaluations:" + multiEvaluationExpert);
		if (multiEvaluationExpert) {
			if (trace)
				logger.trace("Get current Scope");
			MpcNestedConditionsExpertScopeImpl currentScope = getCurrentScope(expert, condition);

			if (trace)
				logger.trace("If Current scope is not null: " + currentScope);
			if (currentScope != null) {
				if (trace)
					logger.trace("Add result to the currentEvaluation state");
				currentScope.setMultiEvaluationExpertState((MpcPotentialMatch) result);
			}
		}
	}

	/**
	 * Returns the scope of the current {@link MpcEvaluator}. If evaluator also
	 * allows nested conditions, then the return scope makes sure that it is not
	 * itself. E.g. if this is the initial condition seq( quant()), if "quant"'s
	 * evaluator invokes this method, it will return scope of "seq" and not
	 * "quant" even if both allow for nested conditions
	 * 
	 * @param expert
	 *            {@link MpcEvaluator} requesting the scope
	 * @param condition
	 *            {@link ModelPathCondition} that is applicable with the
	 *            invoking {@link MpcEvaluator}
	 * @return {@link MpcNestedConditionsExpertScope} representing the scope
	 */
	private <C extends ModelPathCondition> MpcNestedConditionsExpertScopeImpl getCurrentScope(MpcEvaluator<C> expert, C condition) {

		MpcNestedConditionsExpertScopeImpl currentScope = null;

		boolean expertHasNestedConditions = expert.hasNestedConditions(condition);
		boolean multiEvaluationExpert = expert.allowsPotentialMatches(condition);

		if (trace)
			logger.trace("Get scope according to expertHasNestedConditions" + expertHasNestedConditions + "multiEvaluationExpert " + multiEvaluationExpert);
		if (multiEvaluationExpert) {

			if (expertScope != null) {
				if (trace)
					logger.trace(" check if back tracker expert is an other expert invoker (so that it doesn't register itself in its state): " + expertScope);
				if (expertHasNestedConditions) {
					currentScope = expertScope.previous;
				} else {
					currentScope = expertScope;
				}
			}
		}
		if (trace)
			logger.trace("Current Scope:" + currentScope);
		return currentScope;
	}

	@Override
	public <M extends MpcPotentialMatch, C extends ModelPathCondition> M getPreservedState(MpcEvaluator<C> expert, C condition) {

		MpcPotentialMatch result = null;
		boolean multiEvaluationExpert = expert.allowsPotentialMatches(condition);

		if (trace)
			logger.trace("Get preserved state if evaluator allows multiple matching: " + multiEvaluationExpert);
		if (multiEvaluationExpert) {
			MpcNestedConditionsExpertScopeImpl currentScope = getCurrentScope(expert, condition);

			if (currentScope != null) {

				if (trace)
					logger.trace("increment the index of the backtracking evaluator inside the state");
				currentScope.incrementMultiEvaluationExpertIndex();
				if (trace)
					logger.trace("check if this back tracking expert is new or is it a backtracking attempt:" + currentScope.isCurrentMultiEvaluationExpertNew());
				if (!currentScope.isCurrentMultiEvaluationExpertNew()) {
					result = currentScope.getMultiEvaluationExpertState();
					if (trace)
						logger.trace(" Check if this expert will backtrack or just return its last state as it is."
								+ "This is needed when there are multiple backtracking evaluators in the scope of one expert with nested conditions,"
								+ "so that we make sure that all the possible paths are used:" + currentScope.isCurrentMultiEvaluationExpertActive());
					if (currentScope.isCurrentMultiEvaluationExpertActive()) {
						result.setResumptionStrategy(MpcEvaluationResumptionStrategy.resumeProcessing);
					} else {
						result.setResumptionStrategy(MpcEvaluationResumptionStrategy.maintainState);
					}
				}
			}
		}

		if (trace)
			logger.trace("Preserved State: " + result);

		@SuppressWarnings("unchecked")
		M castedResult = (M) result;

		return castedResult;
	}

	@Override
	public void setMpcRegistry(MpcRegistry registry) {
		this.registry = registry;
	}

	@Override
	public MpcRegistry getMpcRegistry() {
		return this.registry;
	}

}
