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
package com.braintribe.model.generic.eval;

import java.util.Optional;
import java.util.stream.Stream;

import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.common.attribute.TypeSafeAttributeEntry;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.processing.async.api.AsyncCallback;

@SuppressWarnings("unusable-by-js")
public class DelegatingEvalContext<R> implements EvalContext<R> {
	protected EvalContext<R> delegate;

	protected DelegatingEvalContext() {
		
	}
	
	public DelegatingEvalContext(EvalContext<R> delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public R get() throws EvalException {
		return getDelegate().get();
	}
	
	@Override
	public void get(AsyncCallback<? super R> callback) {
		getDelegate().get(callback);
	}
	
	@Override
	public Maybe<R> getReasoned() {
		return getDelegate().getReasoned();
	}
	
	@Override
	public void getReasoned(AsyncCallback<? super Maybe<R>> callback) {
		getDelegate().getReasoned(callback);
	}

	@Override
	public <T, A extends EvalContextAspect<? super T>> EvalContext<R> with(Class<A> aspect, T value) {
		return getDelegate().with(aspect, value);
	}

	@Override
	public <A extends TypeSafeAttribute<V>, V> Optional<V> findAttribute(Class<A> attribute) {
		return getDelegate().findAttribute(attribute);
	}

	@Override
	public <A extends TypeSafeAttribute<? super V>, V> void setAttribute(Class<A> attribute, V value) {
		getDelegate().setAttribute(attribute, value);
	}

	@Override
	public <A extends TypeSafeAttribute<V>, V> V getAttribute(Class<A> attribute) {
		return getDelegate().getAttribute(attribute);
	}

	@Override
	public Stream<TypeSafeAttributeEntry> streamAttributes() {
		return getDelegate().streamAttributes();
	}

	protected EvalContext<R> getDelegate() {
		return delegate;
	}
}
