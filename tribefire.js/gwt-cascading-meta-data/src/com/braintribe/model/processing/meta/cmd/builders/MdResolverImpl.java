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
package com.braintribe.model.processing.meta.cmd.builders;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.union;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.ExplicitPredicate;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.meta.data.PredicateErasure;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.Modifiable;
import com.braintribe.model.meta.selector.AccessSelector;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.context.MutableSelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.IsEntityPreliminaryAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.LenienceAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.UseCaseAspect;
import com.braintribe.model.processing.meta.cmd.resolvers.MdAggregator;
import com.braintribe.model.processing.meta.cmd.tools.CmdGwtUtils;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.utils.lcd.CollectionTools2;

@SuppressWarnings("unusable-by-js")
abstract class MdResolverImpl<B extends MdResolver<B>> implements MdResolver<B> {

	protected EntityType<? extends MetaData> metaDataType;
	public MutableSelectorContext selectorContext;
	protected MdAggregator mdAggregator;
	protected final B myself;

	public MdResolverImpl(Class<? extends B> myType, MutableSelectorContext selectorContext, MdAggregator mdAggregator) {
		this.selectorContext = selectorContext;
		this.mdAggregator = mdAggregator;
		this.myself = CmdGwtUtils.cast(myType, this);

	}

	public ModelOracle getModelOracle() {
		return mdAggregator.getModelOracle();
	}

	protected <R extends MdResolver<?>> R lenientOrThrowException(Supplier<? extends R> emptySupplier, Supplier<String> errorMsgSupplier) {
		if (Boolean.TRUE.equals(selectorContext.get(LenienceAspect.class)))
			return emptySupplier.get();
		else
			throw new CascadingMetaDataException(
					"Error while resolving meta-data for model '" + getModelOracle().getGmMetaModel().getName() + "'. " + errorMsgSupplier.get()
							+ ". If this is expected, i.e. model of your CMD resolver may not contain this type, resolve leniently using '.lenient(true)'.");
	}

	@Override
	public boolean is(EntityType<? extends Predicate> predicateType) {
		if (PredicateErasure.T.isAssignableFrom(predicateType))
			throw new IllegalArgumentException(
					"Predicate should not be questioned using the erasure: " + predicateType + ". Use the opposite of this predicate!");

		Predicate predicate = meta(predicateType).exclusive();
		if (predicate != null)
			return isTrue(predicateType, predicate);
		else
			return !ExplicitPredicate.T.isAssignableFrom(predicateType);
	}

	private boolean isTrue(EntityType<? extends Predicate> predicateType, Predicate predicate) {
		if (predicateType == Modifiable.T) {
			Boolean isPreliminary = selectorContext.get(IsEntityPreliminaryAspect.class);
			if (Boolean.TRUE.equals(isPreliminary) && is(Mandatory.T))
				return true;
		}

		return predicate.isTrue();
	}

	@Override
	public B useCase(String useCase) {
		if (useCase != null)
			return useCases(Collections.singleton(useCase));

		return myself;
	}

	@Override
	public B useCases(String... useCases) {
		return useCases(CollectionTools2.asSet(useCases));
	}

	@Override
	public B useCases(Set<String> useCases) {
		if (isEmpty(useCases))
			return myself;

		Set<String> oldUseCases = selectorContext.get(UseCaseAspect.class);
		Set<String> newUseCases = oldUseCases == null ? useCases : union(useCases, oldUseCases);

		selectorContext.put(UseCaseAspect.class, newUseCases);
		return myself;
	}

	@Override
	public B lenient(boolean lenient) {
		selectorContext.put(LenienceAspect.class, lenient);
		return (B) this;
	}

	@Override
	public B access(String externalId) {
		selectorContext.put(AccessAspect.class, externalId);
		return myself;
	}

	@Override
	public B access(AccessSelector accessSelector) {
		selectorContext.put(AccessAspect.class, accessSelector.getExternalId());
		return myself;
	}

	@Override
	public final <T, A extends SelectorContextAspect<? super T>> B with(AspectEntry<T, A> entry) {
		return with(entry.getAspect(), entry.getValue());
	}

	@Override
	public final <T, A extends SelectorContextAspect<? super T>> B with(Class<A> aspect, T value) {
		selectorContext.put(aspect, value);
		return myself;
	}

	@Override
	public B ignoreSelectors() {
		selectorContext.ignoreSelectors();
		return myself;
	}

	@Override
	public B ignoreSelectorsExcept(EntityType<? extends MetaDataSelector>... exceptions) {
		selectorContext.ignoreSelectors(exceptions);
		return myself;
	}

}
