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

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeContextAspect;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.evaluator.api.VdeRegistry;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.builder.VdeRegistryBuilderImpl;

/**
 * Implementation of {@link VdeContext}
 * 
 */
public class VdeContextImpl implements VdeContext {

	private final Map<Class<? extends VdeContextAspect<?>>, Object> aspectValues = new HashMap<Class<? extends VdeContextAspect<?>>, Object>();

	private final Map<ValueDescriptor, Object> cache = new HashMap<ValueDescriptor, Object>();
	private VdeRegistry registry = null;
	private VdeEvaluationMode evaluationMode = VdeEvaluationMode.Final;

	public VdeContextImpl() {
	}

	@Override
	public <T> T evaluate(Object object) throws VdeRuntimeException {
		validateRegistry();
		return evaluate(object, false);
	}

	private void validateRegistry() {
		if (registry == null) {
			registry = new VdeRegistryBuilderImpl().defaultSetup();
		}
	}

	@Override
	public <T> T evaluate(Object object, boolean volatileEvaluation) throws VdeRuntimeException {
		validateRegistry();
		// if unexpected evaluation mode is provided
		if (!(evaluationMode.equals(VdeEvaluationMode.Final) || evaluationMode.equals(VdeEvaluationMode.Preliminary))) {
			throw new VdeRuntimeException("Unexpected evaluation mode is provided" + evaluationMode);
		}

		if (object instanceof ValueDescriptor) {

			Object evaluationResult = null;

			ValueDescriptor valueDescriptor = (ValueDescriptor) object;

			// check cache if it is not volatile
			if (!volatileEvaluation) {

				evaluationResult = this.cache.get(valueDescriptor);
			}

			// if there is no result, compute it
			if (evaluationResult == null) {

				ValueDescriptorEvaluator<ValueDescriptor> expert = null;
				try {
					// findExperts
					expert = findExpertFor(valueDescriptor);

				} catch (VdeRuntimeException e) {
					// In case no expert has been found
					switch (evaluationMode) {
						case Preliminary:
							@SuppressWarnings("unchecked")
							T result = (T) valueDescriptor;
							return result;
						case Final:
							throw e;
					}
				}

				// There is an expert
				VdeResult expertResult = null;
				try {
					// evaluate using expert
					expertResult = expert.evaluate(this, valueDescriptor);
				} catch (VdeRuntimeException e) {
					// if an error is thrown by any expert then it is assumed
					// this expert faced a problem regardless of the evaluation
					// mode
					throw e;
				}

				// if no evaluation was possible
				if (expertResult.isNoEvaluationPossible()) {
					switch (evaluationMode) {
						case Preliminary:
							// return valueDescriptor as is
							@SuppressWarnings("unchecked")
							T result = (T) valueDescriptor;
							return result;
						case Final:
							throw new VdeRuntimeException("Expert [" + expert + "] was not able to evaluate ValueDescriptor [" + valueDescriptor + "], Expert's message: "
									+ expertResult.getNoEvaluationReason());
					}

				} else {
					// evaluation was possible
					evaluationResult = expertResult.getResult();

					if (!expertResult.isVolatileValue()) {
						this.cache.put(valueDescriptor, evaluationResult);
					}
				}

			}
			@SuppressWarnings("unchecked")
			T result = (T) evaluationResult;
			return result;

		}

		@SuppressWarnings("unchecked")
		T result = (T) object;
		return result;
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

		@SuppressWarnings("unchecked")
		ValueDescriptorEvaluator<ValueDescriptor> result = (ValueDescriptorEvaluator<ValueDescriptor>) registry.getConcreteExperts().get(valueDescriptorType.getJavaType());

		// No concrete expert was found, attempt to find an abstract expert
		if (result == null) {

			for (Pair<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>> abstractExpertPair : registry.getAbstractExperts()) {
				Class<? extends ValueDescriptor> abstractExpertJavaClass = abstractExpertPair.getFirst();
				EntityType<? extends ValueDescriptor> abstractExpertType = GMF.getTypeReflection().getEntityType(abstractExpertJavaClass);
				if (abstractExpertType.isAssignableFrom(valueDescriptorType)) {
					ValueDescriptorEvaluator<ValueDescriptor> abstractVde = (ValueDescriptorEvaluator<ValueDescriptor>) abstractExpertPair.getSecond();
					result = abstractVde;

					registry.putConcreteExpert(valueDescriptorType.getJavaType(), abstractVde);

					break;
				}
			}
		}
		if (result == null) {

			throw new VdeRuntimeException("No evaluator found for ValueDescriptor: " + valueDescriptorType.getTypeSignature());

		}

		return result;
	}

	// aspect handling methods
	@SuppressWarnings("unchecked")
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
