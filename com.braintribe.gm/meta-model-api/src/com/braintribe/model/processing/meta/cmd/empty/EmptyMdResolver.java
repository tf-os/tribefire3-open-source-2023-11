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
package com.braintribe.model.processing.meta.cmd.empty;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.ExplicitPredicate;
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.meta.data.PredicateErasure;
import com.braintribe.model.meta.selector.AccessSelector;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.builders.AspectEntry;
import com.braintribe.model.processing.meta.cmd.builders.MdResolver;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;

abstract class EmptyMdResolver<B extends MdResolver<B>> implements MdResolver<B> {

	@Override
	public boolean is(EntityType<? extends Predicate> predicateType) {
		if (PredicateErasure.T.isAssignableFrom(predicateType)) {
			throw new CascadingMetaDataException(
					"Predicate should not be questioned using the erasure: " + predicateType + ". Use the opposite of this predicate!");
		}

		Predicate predicate = meta(predicateType).exclusive();
		if (predicate != null) {
			return predicate.isTrue();
		}

		return !ExplicitPredicate.T.isAssignableFrom(predicateType);
	}

	@Override
	public final B useCase(String useCase) {
		return (B) this;
	}

	@Override
	public final B useCases(String... useCases) {
		return (B) this;
	}

	@Override
	public final B useCases(Set<String> useCases) {
		return (B) this;
	}

	@Override
	public B lenient(boolean lenient) {
		return (B) this;
	}

	@Override
	public final B access(String externalId) {
		return (B) this;
	}

	@Override
	public final B access(AccessSelector accessSelector) {
		return (B) this;
	}

	@Override
	public final <T, A extends SelectorContextAspect<? super T>> B with(AspectEntry<T, A> entry) {
		return (B) this;
	}

	@Override
	public final <T, A extends SelectorContextAspect<? super T>> B with(Class<A> aspect, T value) {
		return (B) this;
	}

	@Override
	public B ignoreSelectors() {
		return (B) this;
	}

	@Override
	public B ignoreSelectorsExcept(EntityType<? extends MetaDataSelector>... exceptions) {
		return (B) this;
	}

	@Override
	public B fork() {
		return (B) this;
	}

}
