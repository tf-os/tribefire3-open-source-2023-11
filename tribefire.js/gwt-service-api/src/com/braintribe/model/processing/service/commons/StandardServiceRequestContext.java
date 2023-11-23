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
package com.braintribe.model.processing.service.commons;



import java.util.Optional;
import java.util.function.Consumer;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.api.ServiceApiConstants;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.ServiceRequestContextAspect;
import com.braintribe.model.processing.service.api.ServiceRequestContextBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.aspect.EagerResponseConsumerAspect;
import com.braintribe.model.processing.service.api.aspect.SummaryLoggerAspect;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.collection.impl.MapAttributeContext;
import com.braintribe.utils.lcd.NullSafe;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * <p>
 * A standard implementation of {@link ServiceRequestContext} which enables context enriching.
 * 
 * @author neidhart.orlich
 * @author dirk.scheffler
 */
@SuppressWarnings("unusable-by-js")
@JsType(namespace = ServiceApiConstants.NAMESPACE_SERVICE)
public class StandardServiceRequestContext extends MapAttributeContext implements ServiceRequestContext {
	
	private ServiceRequestSummaryLogger summaryLogger;
	private Evaluator<ServiceRequest> evaluator;
	
	@JsIgnore
	public StandardServiceRequestContext(Evaluator<ServiceRequest> evaluator) {
		this(null, evaluator);
	}
	
	@JsIgnore
	public StandardServiceRequestContext(ServiceRequestContext parent) {
		this(parent, parent.getEvaluator());
	}
	
	public StandardServiceRequestContext(AttributeContext parent, Evaluator<ServiceRequest> evaluator) {
		super(parent);
		this.evaluator = NullSafe.nonNull(evaluator, "evaluator");
	}
	
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		checkMutability();
		this.evaluator = evaluator;
	}
	
	@Override
	public Evaluator<ServiceRequest> getEvaluator() {
		return evaluator;
	}
	
	@Override
	public ServiceRequestContextBuilder derive() {
		return new StandardServiceRequestContextBuilder(this);
	}

	public void setSummaryLogger(ServiceRequestSummaryLogger summaryLogger) {
		setAttribute(SummaryLoggerAspect.class, summaryLogger);
	}
	
	@Override
	public <T> EvalContext<T> eval(ServiceRequest evaluable) {
		return evaluator.eval(evaluable);
	}

	@Override
	public ServiceRequestSummaryLogger summaryLogger() {
		if (summaryLogger == null) {
			summaryLogger = getAspect(SummaryLoggerAspect.class).orElse(NoOpServiceRequestSummaryLogger.INSTANCE);
		}

		return summaryLogger;
	}

	@Override
	public <T, A extends ServiceRequestContextAspect<? super T>> T findAspect(Class<A> aspect) {
		return findAttribute((Class<TypeSafeAttribute<T>>)(Class<?>) aspect).orElse(null);
	}

	@Override
	public <T, A extends ServiceRequestContextAspect<? super T>> Optional<T> getAspect(Class<A> aspect) {
		return Optional.ofNullable(findAspect(aspect));
	}

	@Override
	public <T, A extends ServiceRequestContextAspect<? super T>> T getAspect(Class<A> aspect, T def) {
		Object value = findAspect(aspect);
		
		if (value == null)
			return def;
		else
			return (T) value;
	}
	
	public Consumer<?> responseConsumer() {
		return findAspect(EagerResponseConsumerAspect.class);
	}
}
