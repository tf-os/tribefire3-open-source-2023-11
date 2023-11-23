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
package com.braintribe.model.processing.vde.evaluator.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.async.client.DeferredExecutor;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.processing.vde.clone.async.AsyncCloningImpl;
import com.braintribe.model.processing.vde.clone.async.DeferredExecutorAspect;
import com.braintribe.model.processing.vde.clone.async.SkipCloningPredicateAspect;
import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeContextAspect;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.evaluator.api.VdeRegistry;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.processing.async.api.AsyncCallback;

/**
 * Implementation of {@link VdeContext}
 */
public class VdeContextImpl implements VdeContext {

	private final Map<Class<? extends VdeContextAspect<?>>, Object> aspectValues = new HashMap<>();

	private final Map<ValueDescriptor, Object> cache = new HashMap<>();
	private final Map<ValueDescriptor, Future<Object>> asyncCache = new HashMap<>();
	private VdeRegistry registry = null;
	private VdeEvaluationMode evaluationMode = VdeEvaluationMode.Final;

	public VdeContextImpl() {
	}

	@Override
	public <T> T evaluate(Object object) throws VdeRuntimeException {
		return evaluate(object, false);
	}

	@Override
	public <T> void evaluate(Object object, AsyncCallback<T> callback) {
		evaluate(object, false, callback);
	}

	@Override
	public <T> void evaluate(Object object, boolean volatileEvaluation, AsyncCallback<T> callback) {
		validateRegistry();

		DeferredExecutor executor = resolveDeferredExecutor();
		Predicate<GenericEntity> skipCloningPredicate = resolveSkipCloningPredicate();
		BiConsumer<ValueDescriptor, AsyncCallback<Object>> vdEvaluator = (vd, _callback) -> evaluateAsync(vd, volatileEvaluation, _callback);

		new AsyncCloningImpl(vdEvaluator, executor, skipCloningPredicate).cloneValue(object, callback);
	}

	private DeferredExecutor resolveDeferredExecutor() {
		DeferredExecutor result = get(DeferredExecutorAspect.class);
		return result != null ? result : DeferredExecutor.gwtDeferredExecutor();
	}

	private Predicate<GenericEntity> resolveSkipCloningPredicate() {
		Predicate<GenericEntity> result = get(SkipCloningPredicateAspect.class);
		return result != null ? result : e -> false;
	}

	private void evaluateAsync(ValueDescriptor valueDescriptor, boolean volatileEvaluation, AsyncCallback<Object> valueConsumer) {
		if (volatileEvaluation) {
			evaluateVolatile(valueDescriptor, valueConsumer);
			return;
		}

		asyncCache.computeIfAbsent(valueDescriptor, vd -> {
			Future<Object> promise = new Future<>();
			evaluateVolatile(vd, promise);
			return promise;

		}).get(Future.asyncGwt(valueConsumer::onFailure, valueConsumer::onSuccess));
	}

	private void evaluateVolatile(ValueDescriptor valueDescriptor, AsyncCallback<Object> valueConsumer) {
		// if there is no result, compute it
		ValueDescriptorEvaluator<ValueDescriptor> expert = null;
		try {
			// findExperts
			expert = findExpertFor(valueDescriptor);

		} catch (VdeRuntimeException e) {
			// In case no expert has been found
			switch (evaluationMode) {
				case Preliminary:
					valueConsumer.onSuccess(valueDescriptor);
					return;
				case Final:
					valueConsumer.onFailure(e);
					return;
			}
		}

		if (expert == null)
			return;
		
		expert.evaluateAsync(this, valueDescriptor, AsyncCallback.of(//
				result -> valueConsumer.onSuccess(result.getResult()), //
				valueConsumer::onFailure));
	}

	@Override
	public <T> T evaluate(Object object, boolean volatileEvaluation) throws VdeRuntimeException {
		validateRegistry();
		// if unexpected evaluation mode is provided
		if (!(evaluationMode.equals(VdeEvaluationMode.Final) || evaluationMode.equals(VdeEvaluationMode.Preliminary)))
			throw new VdeRuntimeException("Unexpected evaluation mode is provided" + evaluationMode);

		if (!(object instanceof ValueDescriptor))
			return (T) object;

		Object evaluationResult = null;

		ValueDescriptor valueDescriptor = (ValueDescriptor) object;

		// check cache if it is not volatile
		if (!volatileEvaluation)
			evaluationResult = this.cache.get(valueDescriptor);

		if (evaluationResult != null)
			return (T) evaluationResult;

		// if there is no result, compute it
		ValueDescriptorEvaluator<ValueDescriptor> expert = null;
		try {
			// findExperts
			expert = findExpertFor(valueDescriptor);

		} catch (VdeRuntimeException e) {
			// In case no expert has been found
			switch (evaluationMode) {
				case Preliminary:
					return (T) valueDescriptor;
				case Final:
					throw e;
			}
		}

		//TODO: what if the expert is null?
		// if an error is thrown by any expert then it is assumed this expert faced a problem regardless of the evaluation mode
		VdeResult expertResult = expert.evaluate(this, valueDescriptor);

		// if no evaluation was possible
		if (expertResult.isNoEvaluationPossible()) {
			switch (evaluationMode) {
				case Preliminary:
					// return valueDescriptor as is
					return (T) valueDescriptor;
				case Final:
					throw new VdeRuntimeException("Expert [" + expert + "] was not able to evaluate ValueDescriptor [" + valueDescriptor
							+ "], Expert's message: " + expertResult.getNoEvaluationReason());
			}

		} else {
			// evaluation was possible
			evaluationResult = expertResult.getResult();

			if (!expertResult.isVolatileValue())
				this.cache.put(valueDescriptor, evaluationResult);
		}

		return (T) evaluationResult;
	}

	private void validateRegistry() {
		if (registry == null)
			registry = VDE.registryBuilder().defaultSetup();
	}

	/**
	 * Identifies the expert needed for the provided valueDescriptor
	 * 
	 * @param valueDescriptor
	 *            The valueDescriptor that requires an expert
	 * @return The evaluator associated with the provided valueDescriptor
	 */
	private ValueDescriptorEvaluator<ValueDescriptor> findExpertFor(ValueDescriptor valueDescriptor) throws VdeRuntimeException {

		EntityType<? extends ValueDescriptor> valueDescriptorType = valueDescriptor.entityType();

		ValueDescriptorEvaluator<ValueDescriptor> result = (ValueDescriptorEvaluator<ValueDescriptor>) registry.getConcreteExperts()
				.get(valueDescriptorType.getJavaType());

		// No concrete expert was found, attempt to find an abstract expert
		if (result == null) {

			for (Pair<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>> abstractExpertPair : registry.getAbstractExperts()) {
				Class<? extends ValueDescriptor> abstractExpertJavaClass = abstractExpertPair.getFirst();
				EntityType<? extends ValueDescriptor> abstractExpertType = GMF.getTypeReflection().getEntityType(abstractExpertJavaClass);
				if (abstractExpertType.isAssignableFrom(valueDescriptorType)) {
					ValueDescriptorEvaluator<ValueDescriptor> abstractVde = (ValueDescriptorEvaluator<ValueDescriptor>) abstractExpertPair
							.getSecond();
					result = abstractVde;

					registry.putConcreteExpert(valueDescriptorType.getJavaType(), abstractVde);

					break;
				}
			}
		}
		if (result == null)
			throw new VdeRuntimeException("No evaluator found for ValueDescriptor: " + valueDescriptorType.getTypeSignature());

		return result;
	}

	// aspect handling methods
	@Override
	public <T, A extends VdeContextAspect<T>> T get(Class<A> aspect) {
		return (T) aspectValues.get(aspect);
	}

	@Override
	public <T, A extends VdeContextAspect<? super T>> void put(Class<A> aspect, T value) {
		aspectValues.put(aspect, value);
	}

	@Override
	public void setEvaluationMode(VdeEvaluationMode mode) {
		this.evaluationMode = mode;
	}

	@Override
	public VdeEvaluationMode getEvaluationMode() {
		return this.evaluationMode;
	}

	@Override
	public void setVdeRegistry(VdeRegistry registry) {
		this.registry = registry;

	}

	@Override
	public VdeRegistry getVdeRegistry() {
		return registry;
	}
}
