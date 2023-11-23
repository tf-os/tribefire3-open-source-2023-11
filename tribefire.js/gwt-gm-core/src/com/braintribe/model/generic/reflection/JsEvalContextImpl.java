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
package com.braintribe.model.generic.reflection;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.common.attribute.TypeSafeAttributeEntry;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalContextAspect;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.JsEvalContext;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.JsPromise;
import com.braintribe.utils.promise.JsPromiseCallback;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class JsEvalContextImpl<R> implements JsEvalContext<R> {

	private final EvalContext<R> delegate;

	public JsEvalContextImpl(EvalContext<R> delegate) {
		this.delegate = delegate;
	}

	@Override
	public JsPromise<R> andGet() {
		return JsPromiseCallback.promisify(this::get);
	}

	@Override
	public JsPromise<Maybe<R>> andGetReasoned() {
		return JsPromiseCallback.promisify(this::getReasoned);
	}

	@Override
	public <A extends TypeSafeAttribute<V>, V> V findOrNull(Class<A> attribute) {
		return delegate.findOrNull(attribute);
	}

	@Override
	public <A extends TypeSafeAttribute<V>, V> V findOrDefault(Class<A> attribute, V defaultValue) {
		return delegate.findOrDefault(attribute, defaultValue);
	}

	@Override
	public <A extends TypeSafeAttribute<V>, V> V findOrSupply(Class<A> attribute, Supplier<V> defaultValueSupplier) {
		return delegate.findOrSupply(attribute, defaultValueSupplier);
	}

	@Override
	public R get() throws EvalException {
		return delegate.get();
	}

	@Override
	public void get(AsyncCallback<? super R> callback) {
		delegate.get(callback);
	}

	@Override
	public Maybe<R> getReasoned() {
		return delegate.getReasoned();
	}

	@Override
	public void getReasoned(AsyncCallback<? super Maybe<R>> callback) {
		delegate.getReasoned(callback);
	}

	@Override
	public <T, A extends EvalContextAspect<? super T>> EvalContext<R> with(Class<A> aspect, T value) {
		return delegate.with(aspect, value);
	}

	@Override
	public <A extends TypeSafeAttribute<V>, V> Optional<V> findAttribute(Class<A> attribute) {
		return delegate.findAttribute(attribute);
	}

	@Override
	public <A extends TypeSafeAttribute<? super V>, V> void setAttribute(Class<A> attribute, V value) {
		delegate.setAttribute(attribute, value);
	}

	@Override
	public <A extends TypeSafeAttribute<V>, V> V getAttribute(Class<A> attribute) {
		return delegate.getAttribute(attribute);
	}

	@Override
	public Stream<TypeSafeAttributeEntry> streamAttributes() {
		return delegate.streamAttributes();
	}

}
