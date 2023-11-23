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

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.meta.data.PredicateErasure;
import com.braintribe.model.meta.selector.AccessSelector;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.LenienceAspect;
import com.braintribe.model.processing.meta.cmd.extended.MdDescriptor;
import com.braintribe.model.processing.meta.cmd.result.MdResult;

/**
 * Common base for metaData context builders - builders which are used to specify what metaData we want to resolve and also the
 * {@link SelectorContext} to be used for resolution.
 * 
 * @param <B>
 *            builder class (the result of {@code this.getClass()} should be {@code Class<B>)}
 */
public interface MdResolver<B extends MdResolver<B>> {

	/** Sets the type of meta data to be resolved (e.g. EntityVisibility). */
	<M extends MetaData> MdResult<M, ? extends MdDescriptor> meta(EntityType<M> metaDataType);

	/**
	 * Resolves given {@link Predicate} MD to the correct boolean value (see the Predicate's documentation). Note you have to resolve the predicate
	 * here, and never the {@link PredicateErasure}. passing an erasure to this method results in a {@link IllegalArgumentException}.
	 */
	boolean is(EntityType<? extends Predicate> predicateType);

	/** Appends a useCase to the current resolution context. Null value given here is ignored. */
	B useCase(String useCase);

	/** Appends given useCases to the current resolution context. Given array cannot be null. */
	B useCases(String... useCases);

	/** Appends given useCases to the current resolution context. Given {@link Set} cannot be null. */
	B useCases(Set<String> useCases);

	/**
	 * When set to <code>true</code>, the resolver doesn't throw exceptions due to types not found in the underlying model, but simply resolves no
	 * meta-data.
	 * <p>
	 * Note that this internally sets the value for the {@link LenienceAspect}, so if you want to turn lenient resolving on by default in the
	 * resolver, simply configure this as a static aspect (I know this is implementation specific but OK).
	 */
	B lenient(boolean lenient);

	B access(String externalId);

	B access(AccessSelector accessSelector);

	/** Sets any {@link SelectorContextAspect} with corresponding value. */
	<T, A extends SelectorContextAspect<? super T>> B with(AspectEntry<T, A> entry);

	/** Sets any {@link SelectorContextAspect} with corresponding value. */
	<T, A extends SelectorContextAspect<? super T>> B with(Class<A> aspect, T value);

	/**
	 * If specified, the MD is resolved as if there ware no {@link MetaDataSelector}s configured. This specifically means that {@link MdResult#list()}
	 * can be used to retrieve all individual instances of given meta-data configured on the corresponding model element.
	 * 
	 * @see #ignoreSelectorsExcept(EntityType...)
	 */
	B ignoreSelectors();

	/**
	 * Similar to {@link #ignoreSelectors()}, but given {@link MetaDataSelector}s are the only ones that are actually evaluated, while the other ones
	 * are ignored. Note that logical selectors are automatically among exceptions and don't have to be listed here.
	 */
	B ignoreSelectorsExcept(EntityType<? extends MetaDataSelector>... exceptions);

	/**
	 * Returns a copy of this mdResolver whose state is independent on this one. Because building the resolution context (with methods like
	 * {@link #useCase(String)} or {@link #ignoreSelectors()}) mutates the state of the context, we provide an explicit option get a copy and thus
	 * allow resolution which will not affect the context of the original resolver.
	 * <p>
	 * For example if you want to use the same context but sometimes you want to ignore selectors, this is not possible without this method, as
	 * calling {@link #ignoreSelectors()} cannot be undone on a context.
	 */
	B fork();

}
